package org.flightofstairs.adirstat.view.packing;

import android.graphics.Rect;
import com.google.common.collect.ImmutableSortedSet;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.view.DisplayNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

import static org.junit.Assert.assertEquals;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SquarifiedPackingTest {

    private static final ImmutableSortedSet<Tree<DisplayNode>> EMPTY_DISPLAY = ImmutableSortedSet.of();

    @Test
    public void testPack() throws Exception {
        Tree<DisplayNode> expected =
                new Tree<>(new DisplayNode(new File("root"), new Rect(0, 0, 800, 300)), ImmutableSortedSet.of(
                        new Tree<>(new DisplayNode(new File("root/12byteFile.txt"), new Rect(5, 5, 395, 295)), EMPTY_DISPLAY),
                        new Tree<>(new DisplayNode(new File("root/dir1"), new Rect(405, 5, 795, 295)), ImmutableSortedSet.of(
                                new Tree<>(new DisplayNode(new File("root/dir1/6byteFile.txt"), new Rect(409, 9, 596, 291)), EMPTY_DISPLAY),
                                new Tree<>(new DisplayNode(new File("root/dir1/dir2"), new Rect(604, 9, 791, 291)), ImmutableSortedSet.of(
                                        new Tree<>(new DisplayNode(new File("root/dir1/dir2/3byteFile.txt"), new Rect(607, 12, 788, 147)), EMPTY_DISPLAY),
                                        new Tree<>(new DisplayNode(new File("root/dir1/dir2/3byteFile2.txt"), new Rect(607, 153, 788, 288)), EMPTY_DISPLAY)))))));

        Tree<DisplayNode> actual = SquarifiedPacking.pack(TestTrees.FS_SUMMARY_TREE, new Rect(0, 0, 800, 300));

        assertEquals(expected, actual);
    }
}