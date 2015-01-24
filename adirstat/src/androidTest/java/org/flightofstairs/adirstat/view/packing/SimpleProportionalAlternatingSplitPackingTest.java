package org.flightofstairs.adirstat.view.packing;

import android.graphics.Rect;
import junit.framework.Assert;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.view.DisplayNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.flightofstairs.adirstat.view.packing.SimpleProportionalAlternatingSplitPacking.Scaling;
import static org.flightofstairs.adirstat.view.packing.TestTrees.DISPLAYNODE_TREE;
import static org.flightofstairs.adirstat.view.packing.TestTrees.FS_SUMMARY_TREE;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SimpleProportionalAlternatingSplitPackingTest {

    @Test
    public void testPack() throws Exception {
        Tree<DisplayNode> actual = new SimpleProportionalAlternatingSplitPacking(Scaling.LINEAR).pack(FS_SUMMARY_TREE, new Rect(0, 0, 800, 300));
        Assert.assertEquals(DISPLAYNODE_TREE, actual);
    }
}