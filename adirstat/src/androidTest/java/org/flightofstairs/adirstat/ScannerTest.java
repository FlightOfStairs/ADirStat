package org.flightofstairs.adirstat;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

import static com.google.common.base.Charsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class ScannerTest {
    public static final ImmutableSortedSet<FsNode> EMPTY = ImmutableSortedSet.of();

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

    @SneakyThrows
    @Test
    public void testSingle() {
        File root = testFolder.getRoot();
        Files.write("hello", new File(root, "hello.txt"), UTF_8);

        Optional<FsNode> possibleNode = Scanner.recursiveList(root);
        assertTrue(possibleNode.isPresent());

        FsNode file = new FsNode("hello.txt", EMPTY, 5, 0);
        FsNode dir = new FsNode(root.getName(), ImmutableSortedSet.of(file), 5, 1);

        assertEquals(dir, possibleNode.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SneakyThrows
    @Test
    public void testComplex() {
        File root = testFolder.getRoot();

        Files.write("hello", new File(root, "hello.txt"), UTF_8);
        Files.write("world", new File(root, "world.txt"), UTF_8);

        File second = new File(root, "second");
        second.mkdir();

        Files.write("1", new File(second, "1.txt"), UTF_8);

        File third = new File(second, "third");
        third.mkdir();

        Files.write("22", new File(third, "2.txt"), UTF_8);
        Files.write("333", new File(third, "3.txt"), UTF_8);

        File fourth = new File(root, "fourth");
        fourth.mkdir();

        Files.write("4444", new File(fourth, "4.txt"), UTF_8);

        Optional<FsNode> possibleNode = Scanner.recursiveList(root);
        assertTrue(possibleNode.isPresent());

        FsNode dir = new FsNode(root.getName(), ImmutableSortedSet.of(
                new FsNode("hello.txt", EMPTY, 5, 1),
                new FsNode("world.txt", EMPTY, 5, 1),
                new FsNode("second", ImmutableSortedSet.of(
                        new FsNode("1.txt", EMPTY, 1, 1),
                        new FsNode("third", ImmutableSortedSet.of(
                                new FsNode("2.txt", EMPTY, 2, 1),
                                new FsNode("3.txt", EMPTY, 3, 1)
                        ), 5, 2)
                ), 6, 3),
                new FsNode("fourth", ImmutableSortedSet.of(
                        new FsNode("4.txt", ImmutableSortedSet.of(), 4, 1)
                ), 4, 1)
        ), 20, 6);

        assertEquals(dir, possibleNode.get());
    }
}
