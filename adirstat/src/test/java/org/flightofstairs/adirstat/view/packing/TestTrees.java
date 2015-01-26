package org.flightofstairs.adirstat.view.packing;

import android.graphics.Rect;
import com.google.common.collect.ImmutableSortedSet;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.model.FilesystemSummary;
import org.flightofstairs.adirstat.view.DisplayNode;

import java.io.File;

public class TestTrees {
    private static final ImmutableSortedSet<Tree<FilesystemSummary>> EMPTY_FS = ImmutableSortedSet.of();
    private static final ImmutableSortedSet<Tree<DisplayNode>> EMPTY_DISPLAY = ImmutableSortedSet.of();

    public static final Tree<FilesystemSummary> FS_SUMMARY_TREE =
            new Tree<>(new FilesystemSummary(new File("root"), 24, 4), ImmutableSortedSet.of(
                new Tree<>(new FilesystemSummary(new File("root/12byteFile.txt"), 12, 1), EMPTY_FS),
                new Tree<>(new FilesystemSummary(new File("root/dir1"), 12, 3), ImmutableSortedSet.of(
                        new Tree<>(new FilesystemSummary(new File("root/dir1/6byteFile.txt"), 6, 1), EMPTY_FS),
                        new Tree<>(new FilesystemSummary(new File("root/dir1/dir2"), 6, 2), ImmutableSortedSet.of(
                                new Tree<>(new FilesystemSummary(new File("root/dir1/dir2/3byteFile.txt"), 3, 1), EMPTY_FS),
                                new Tree<>(new FilesystemSummary(new File("root/dir1/dir2/3byteFile2.txt"), 3, 1), EMPTY_FS)))))));

    public static final Tree<DisplayNode> DISPLAYNODE_TREE =
            new Tree<>(new DisplayNode(new File("root"), new Rect(0, 0, 800, 300)), ImmutableSortedSet.of(
                    new Tree<>(new DisplayNode(new File("root/12byteFile.txt"), new Rect(0, 0, 400, 300)), EMPTY_DISPLAY),
                    new Tree<>(new DisplayNode(new File("root/dir1"), new Rect(400, 0, 800, 300)), ImmutableSortedSet.of(
                            new Tree<>(new DisplayNode(new File("root/dir1/6byteFile.txt"), new Rect(400, 0, 800, 150)), EMPTY_DISPLAY),
                            new Tree<>(new DisplayNode(new File("root/dir1/dir2"), new Rect(400, 150, 800, 300)), ImmutableSortedSet.of(
                                    new Tree<>(new DisplayNode(new File("root/dir1/dir2/3byteFile.txt"), new Rect(400, 150, 600, 300)), EMPTY_DISPLAY),
                                    new Tree<>(new DisplayNode(new File("root/dir1/dir2/3byteFile2.txt"), new Rect(600, 150, 800, 300)), EMPTY_DISPLAY)))))));

}
