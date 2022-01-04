package org.flightofstairs.adirstat.model;

import static com.google.common.base.Verify.verifyNotNull;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.transform;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Locale.UK;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.os.AsyncTask;
import android.system.Os;
import android.system.StructStat;
import android.util.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.squareup.otto.Bus;

import org.flightofstairs.adirstat.Tree;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

import lombok.SneakyThrows;

public class Scanner extends AsyncTask<File, Void, Optional<Tree<FilesystemSummary>>> {

    private static final int MIN_FILE_BYTES = 1;

    public static final Semaphore ADDITIONAL_THREADS = new Semaphore(10);

    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
    public static final ExecutorService DIRECT_THREAD = MoreExecutors.newDirectExecutorService();

    private final Bus bus;

    @Inject
    public Scanner(Bus bus) {
        this.bus = bus;
    }

    @Override
    @SneakyThrows
    protected Optional<Tree<FilesystemSummary>> doInBackground(File... params) {
        File root = verifyNotNull(params[0]);

        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<Tree<FilesystemSummary>> node = recursiveList(root.getCanonicalFile(), Scanner::smartLength);

        String logMessage = node.isPresent()
                ? String.format(UK, "Found %d files (%dmb) in %ds.", node.get().getValue().getSubTreeCount(), node.get().getValue().getSubTreeBytes() / (int) Math.pow(1024, 2), stopwatch.elapsed(SECONDS))
                : "Failed to list files.";
        Log.d(getClass().getSimpleName(), logMessage);

        return node;
    }

    @Override
    public void onPostExecute(Optional<Tree<FilesystemSummary>> result) { bus.post(result); }

    @Override
    public void onCancelled() { bus.post(Optional.<Tree<FilesystemSummary>>absent()); }

    @VisibleForTesting
    @SneakyThrows
    // fileSizeMeasure required as Os.stat() returns a stub when testing.
    static Optional<Tree<FilesystemSummary>> recursiveList(File path, Function<File, Long> fileSizeMeasure) {
        if (path.isDirectory()) {
            long subTreeBytes = 0;
            long subTreeCount = 0;

            Iterable<Optional<Tree<FilesystemSummary>>> possibleChildren = listChildrenThreaded(path, fileSizeMeasure);

            SortedSet<Tree<FilesystemSummary>> children = ImmutableSortedSet.copyOf(Optional.presentInstances(possibleChildren));
            for (Tree<FilesystemSummary> child : children) {
                subTreeBytes += child.getValue().getSubTreeBytes();
                subTreeCount += child.getValue().getSubTreeCount();
            }

            return Optional.of(new Tree<>(new FilesystemSummary(path, subTreeBytes, subTreeCount), children));
        } else if (path.isFile()) {
            long length = fileSizeMeasure.apply(path);
            return Optional.of(new Tree<>(new FilesystemSummary(path, max(length, MIN_FILE_BYTES), 1), ImmutableSortedSet.<Tree<FilesystemSummary>>of()));
        }
        return Optional.absent();
    }

    @SneakyThrows
    private static Iterable<Optional<Tree<FilesystemSummary>>> listChildrenThreaded(File path, Function<File, Long> fileSizeMeasure) {
        List<Callable<Optional<Tree<FilesystemSummary>>>> toList = transform(copyOf(path.listFiles()), file -> (Callable<Optional<Tree<FilesystemSummary>>>) () -> Scanner.recursiveList(file, fileSizeMeasure));

        // Using semaphore to permit use of thread pool. If can't acquire, make progress with direct thread. Cannot deadlock.
        boolean threaded = ADDITIONAL_THREADS.tryAcquire();
        ExecutorService service = threaded ? THREAD_POOL : DIRECT_THREAD;

        List<Optional<Tree<FilesystemSummary>>> possibleChildren = new ArrayList<>();
        for (Future<Optional<Tree<FilesystemSummary>>> listing : service.invokeAll(toList)) { // Can't use transform without catching ExecutionException in lambda. :(
            possibleChildren.add(listing.get());
        }

        if (threaded) ADDITIONAL_THREADS.release();
        return possibleChildren;
    }

    // Handle sparse files, such as .thumbdata3: http://android.stackexchange.com/questions/63993/thumbdata-files-maxing-out-internal-sd-memory
    @SneakyThrows
    private static long smartLength(File file) {
        long length = file.length();

        StructStat stat = Os.stat(file.getAbsolutePath());
        long blockBasedLength = stat.st_blksize * stat.st_blocks;
        return min(length, blockBasedLength);
    }
}
