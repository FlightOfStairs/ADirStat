package org.flightofstairs.adirstat;

import com.google.common.base.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ScannerTest {
    @Rule public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testEmpty() {
        Optional<FsNode> possibleNode = Scanner.recursiveList(testFolder.getRoot());
        assertTrue(possibleNode.isPresent());
        FsNode node = possibleNode.get();

        assertEquals(testFolder.getRoot().getName(), node.getName());
        assertEquals(0, node.getSubTreeBytes());
        assertEquals(0, node.getSubTreeCount());
        assertEquals(0, node.getChildren().size());
    }
}
