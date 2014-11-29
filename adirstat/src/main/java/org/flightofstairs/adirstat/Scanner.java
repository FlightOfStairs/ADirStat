package org.flightofstairs.adirstat;

import android.os.AsyncTask;
import android.util.Log;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSortedSet;
import lombok.SneakyThrows;

import java.io.File;
import java.util.SortedSet;

import static com.google.common.base.Verify.verifyNotNull;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;
import static java.util.Locale.UK;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Scanner extends AsyncTask<File, Void, Optional<FsNode>> {

    @Override
    @SneakyThrows
    protected Optional<FsNode> doInBackground(File... params) {
        File root = verifyNotNull(params[0]);

        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<FsNode> node = recursiveList(root.getCanonicalFile());

        String logMessage = node.isPresent()
                ? String.format(UK, "Found %d files (%dmb) in %ds.", node.get().getSubTreeCount(), node.get().getSubTreeBytes() / (int) Math.pow(1024, 2), stopwatch.elapsed(SECONDS))
                : "Failed to list files.";
        Log.d(getClass().getSimpleName(), logMessage);

        return node;
    }

    private static Optional<FsNode> recursiveList(File root) {
        if (root.isDirectory()) {
            long subTreeBytes = 0;
            long subTreeCount = 0;

            Iterable<Optional<FsNode>> possibleChildren = transform(copyOf(root.listFiles()), Scanner::recursiveList);

            SortedSet<FsNode> children = ImmutableSortedSet.copyOf(Optional.presentInstances(possibleChildren));
            for (FsNode child : children) {
                subTreeBytes += child.getSubTreeBytes();
                subTreeCount += child.getSubTreeCount();
            }

            return Optional.of(new FsNode(root.getName(), children, subTreeBytes, subTreeCount));
        } else if (root.isFile()) {
            Log.v("Scanner", "Found " + root);
            return Optional.of(new FsNode(root.getName(), ImmutableSortedSet.<FsNode>of(), root.length(), 1));
        }
        return Optional.absent();
    }
}
