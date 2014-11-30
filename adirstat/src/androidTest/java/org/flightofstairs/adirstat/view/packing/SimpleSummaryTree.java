package org.flightofstairs.adirstat.view.packing;

import com.google.common.collect.ImmutableSortedSet;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.model.FilesystemSummary;

import java.io.File;

public class SimpleSummaryTree {
    private static final ImmutableSortedSet<Tree<FilesystemSummary>> EMPTY_FS = ImmutableSortedSet.of();

    public static Tree<FilesystemSummary> TREE =
            new Tree<>(new FilesystemSummary(new File("root"), 24, 4), ImmutableSortedSet.of(
                new Tree<>(new FilesystemSummary(new File("root/12byteFile.txt"), 12, 1), EMPTY_FS),
                new Tree<>(new FilesystemSummary(new File("root/dir1"), 12, 3), ImmutableSortedSet.of(
                        new Tree<>(new FilesystemSummary(new File("root/dir1/6byteFile.txt"), 6, 1), EMPTY_FS),
                        new Tree<>(new FilesystemSummary(new File("root/dir1/dir2"), 6, 2), ImmutableSortedSet.of(
                                new Tree<>(new FilesystemSummary(new File("root/dir1/dir2/3byteFile.txt"), 3, 1), EMPTY_FS),
                                new Tree<>(new FilesystemSummary(new File("root/dir1/dir2/3byteFile2.txt"), 3, 1), EMPTY_FS)))))));
}
