package org.flightofstairs.adirstat.model;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
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
import java.util.Map;

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

        assertEquals(new Tree<>(new FilesystemSummary(testFolder.getRoot(), 0, 0), EMPTY), node);
    }

    @SneakyThrows
    @Test
    public void testSingle() {
        File root = testFolder.getRoot();
        Files.write("hello", new File(root, "hello.txt"), UTF_8);

        Optional<Tree<FilesystemSummary>> possibleNode = Scanner.recursiveList(root);
        assertTrue(possibleNode.isPresent());

        Tree<FilesystemSummary> file = new Tree<>(new FilesystemSummary(new File(root, "hello.txt"), 5, 1), EMPTY);
        Tree<FilesystemSummary> dir = new Tree<>(new FilesystemSummary(root, 5, 1), ImmutableSortedSet.of(file));

        assertEquals(dir, possibleNode.get());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @SneakyThrows
    @Test
    public void testComplex() {
        File root = testFolder.getRoot();

        Map<String, String> filesContents = ImmutableMap.<String, String>builder()
                .put("hello.txt", "hello")
                .put("world.txt", "world")
                .put("second/1.txt", "1")
                .put("second/third/2.txt", "22")
                .put("second/third/3.txt", "333")
                .put("fourth/4.txt", "4444")
                .build();

        for (Map.Entry<String, String> fileContent : filesContents.entrySet()) {
            File file = new File(root, fileContent.getKey());
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();

            Files.write(fileContent.getValue(), file, UTF_8);
        }

        Optional<Tree<FilesystemSummary>> possibleNode = Scanner.recursiveList(root);
        assertTrue(possibleNode.isPresent());

        // IDEA went a bit mental when this was inline.
        Tree<FilesystemSummary> thirdTree = new Tree<>(new FilesystemSummary(new File(root, "second/third"), 5, 2), ImmutableSortedSet.of(
                new Tree<>(new FilesystemSummary(new File(root, "second/third/2.txt"), 2, 1), EMPTY),
                new Tree<>(new FilesystemSummary(new File(root, "second/third/3.txt"), 3, 1), EMPTY)));

        Tree<FilesystemSummary> dir = new Tree<>(new FilesystemSummary(root, 20, 6), ImmutableSortedSet.of(
                new Tree<>(new FilesystemSummary(new File(root, "hello.txt"), 5, 1), EMPTY),
                new Tree<>(new FilesystemSummary(new File(root, "world.txt"), 5, 1), EMPTY),
                new Tree<>(new FilesystemSummary(new File(root, "second"), 6, 3), ImmutableSortedSet.of(new Tree<>(new FilesystemSummary(new File(root, "second/1.txt"), 1, 1), EMPTY), thirdTree)),
                new Tree<>(new FilesystemSummary(new File(root, "fourth"), 4, 1), ImmutableSortedSet.of(new Tree<>(new FilesystemSummary(new File(root, "fourth/4.txt"), 4, 1), EMPTY)))));

        assertEquals(dir, possibleNode.get());
    }
}
