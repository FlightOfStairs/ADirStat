package org.flightofstairs.adirstat.model;

import android.os.AsyncTask;
import android.util.Log;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSortedSet;
import com.google.inject.Inject;
import com.squareup.otto.Bus;
import lombok.SneakyThrows;
import org.flightofstairs.adirstat.Tree;

import java.io.File;
import java.util.SortedSet;

import static com.google.common.base.Verify.verifyNotNull;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;
import static java.util.Locale.UK;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Scanner extends AsyncTask<File, Void, Optional<Tree<FilesystemSummary>>> {
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
        Optional<Tree<FilesystemSummary>> node = recursiveList(root.getCanonicalFile());

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
    static Optional<Tree<FilesystemSummary>> recursiveList(File root) {
        if (root.isDirectory()) {
            long subTreeBytes = 0;
            long subTreeCount = 0;

            Iterable<Optional<Tree<FilesystemSummary>>> possibleChildren = transform(copyOf(root.listFiles()), Scanner::recursiveList);

            SortedSet<Tree<FilesystemSummary>> children = ImmutableSortedSet.copyOf(Optional.presentInstances(possibleChildren));
            for (Tree<FilesystemSummary> child : children) {
                subTreeBytes += child.getValue().getSubTreeBytes();
                subTreeCount += child.getValue().getSubTreeCount();
            }

            return Optional.of(new Tree<>(new FilesystemSummary(root.getName(), subTreeBytes, subTreeCount), children));
        } else if (root.isFile()) {
            Log.v("Scanner", "Found " + root);
            return Optional.of(new Tree<>(new FilesystemSummary(root.getName(), root.length(), 1), ImmutableSortedSet.<Tree<FilesystemSummary>>of()));
        }
        return Optional.absent();
    }
}