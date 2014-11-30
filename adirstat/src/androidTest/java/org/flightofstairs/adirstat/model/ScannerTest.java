package org.flightofstairs.adirstat.model;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.io.Files;
import lombok.SneakyThrows;
import org.flightofstairs.adirstat.Tree;
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
    public static final ImmutableSortedSet<Tree<FilesystemSummary>> EMPTY = ImmutableSortedSet.of();

    @Rule public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testEmpty() {
        Optional<Tree<FilesystemSummary>> possibleNode = Scanner.recursiveList(testFolder.getRoot());
        assertTrue(possibleNode.isPresent());
        Tree<FilesystemSummary> node = possibleNode.get();

        assertEquals(new Tree<>(new FilesystemSummary(testFolder.getRoot().getName(), 0, 0), EMPTY), node);
    }

    @SneakyThrows
    @Test
    public void testSingle() {
        File root = testFolder.getRoot();
        Files.write("hello", new File(root, "hello.txt"), UTF_8);

        Optional<Tree<FilesystemSummary>> possibleNode = Scanner.recursiveList(root);
        assertTrue(possibleNode.isPresent());

        Tree<FilesystemSummary> file = new Tree<>(new FilesystemSummary("hello.txt", 5, 0), EMPTY);
        Tree<FilesystemSummary> dir = new Tree<>(new FilesystemSummary(root.getName(), 5, 1), ImmutableSortedSet.of(file));

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

        Optional<Tree<FilesystemSummary>> possibleNode = Scanner.recursiveList(root);
        assertTrue(possibleNode.isPresent());

        // IDEA went a bit mental when this was inline.
        Tree<FilesystemSummary> thirdTree = new Tree<>(new FilesystemSummary("third", 5, 2), ImmutableSortedSet.of(
                new Tree<>(new FilesystemSummary("2.txt", 2, 1), EMPTY),
                new Tree<>(new FilesystemSummary("3.txt", 3, 1), EMPTY)));

        Tree<FilesystemSummary> dir = new Tree<>(new FilesystemSummary(root.getName(), 20, 6), ImmutableSortedSet.of(
                new Tree<>(new FilesystemSummary("hello.txt", 5, 1), EMPTY),
                new Tree<>(new FilesystemSummary("world.txt", 5, 1), EMPTY),
                new Tree<>(new FilesystemSummary("second", 4, 1), ImmutableSortedSet.of(new Tree<>(new FilesystemSummary("1.txt", 1, 1), EMPTY), thirdTree)),
                new Tree<>(new FilesystemSummary("fourth", 4, 1), ImmutableSortedSet.of(new Tree<>(new FilesystemSummary("4.txt", 4, 1), EMPTY)))));

        assertEquals(dir, possibleNode.get());
    }
}
