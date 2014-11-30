package org.flightofstairs.adirstat.view.packing;


import android.graphics.Rect;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.model.FilesystemSummary;
import org.flightofstairs.adirstat.view.DisplayNode;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Set;

import static com.google.common.base.Verify.verify;
import static org.flightofstairs.adirstat.view.packing.PackingUtils.Split;
import static org.flightofstairs.adirstat.view.packing.PackingUtils.newBounds;

/**
 * An implementation of the "Squarified Treemaps": http://www.win.tue.nl/~vanwijk/stm.pdf
 */
public class SquarifiedPacker implements Packer {
    private static final Comparator<Tree<FilesystemSummary>> DESCENDING_SUMMARY_TREES = (a, b) ->
            ComparisonChain.start()
                .compare(b.getValue().getSubTreeBytes(), a.getValue().getSubTreeBytes())
                .compare(a.getValue().getPath(), b.getValue().getPath()).result();

    @Nonnull
    @Override
    public Tree<DisplayNode> pack(@Nonnull Tree<FilesystemSummary> summaryTree, @Nonnull Rect bounds) {
        return new Tree<>(new DisplayNode(summaryTree.getValue().getPath(), bounds), packChildren(summaryTree.getChildren(), bounds, summaryTree.getValue().getSubTreeBytes()));
    }

    @Nonnull
    public ImmutableSortedSet<Tree<DisplayNode>> packChildren(@Nonnull Collection<Tree<FilesystemSummary>> summaryTrees, @Nonnull Rect bounds, long totalBytes) {
        if (summaryTrees.isEmpty()) return ImmutableSortedSet.of();

        NavigableSet<Tree<FilesystemSummary>> descendingSize = ImmutableSortedSet.copyOf(DESCENDING_SUMMARY_TREES, summaryTrees);

        NavigableSet<Tree<FilesystemSummary>> row = firstRow(bounds, totalBytes, descendingSize);

        verify(!row.isEmpty());

        long rowTotalBytes = 0;
        for (Tree<FilesystemSummary> summaryTree : row) rowTotalBytes += summaryTree.getValue().getSubTreeBytes();

        double rowFraction = totalBytes == 0 ? 0 : rowTotalBytes / (double) totalBytes;

        ImmutableSortedSet.Builder<Tree<DisplayNode>> displayTrees = ImmutableSortedSet.naturalOrder();

        NavigableSet<Tree<FilesystemSummary>> remaining = descendingSize.tailSet(row.last(), false);

        verify(remaining.size() + row.size() == summaryTrees.size());

        Split split = Split.forBounds(bounds);

        displayTrees.addAll(placeRow(row, newBounds(bounds, split, rowFraction, 0), rowTotalBytes));
        displayTrees.addAll(packChildren(remaining, newBounds(bounds, split, 1 - rowFraction, rowFraction), totalBytes - rowTotalBytes));

        return displayTrees.build();
    }

    @Nonnull
    private Set<Tree<DisplayNode>> placeRow(@Nonnull Iterable<Tree<FilesystemSummary>> row, @Nonnull Rect rowBounds, long rowTotalBytes) {
        Split split = Split.forBounds(rowBounds);

        ImmutableSortedSet.Builder<Tree<DisplayNode>> rowChildren = ImmutableSortedSet.naturalOrder();

        double priorFraction = 0;
        for (Tree<FilesystemSummary> summaryTree : row) {
            double fraction = rowTotalBytes == 0 ? 0 : summaryTree.getValue().getSubTreeBytes() / (double) rowTotalBytes;

            rowChildren.add(pack(summaryTree, newBounds(rowBounds, split, fraction, priorFraction)));
            priorFraction += fraction;
        }

        return rowChildren.build();
    }

    @Nonnull
    private NavigableSet<Tree<FilesystemSummary>> firstRow(Rect bounds, long totalBytes, NavigableSet<Tree<FilesystemSummary>> descendingSize) {
        int width = Math.max(bounds.width(), bounds.height());

        NavigableSet<Tree<FilesystemSummary>> row = descendingSize.headSet(descendingSize.first(), true);

        // Expand row while adding next element would improve aspect ratios
        NavigableSet<Tree<FilesystemSummary>> expandedRow;
        while (descendingSize.higher(row.last()) != null
                && worst(areas(row, bounds, totalBytes), width) > worst(areas((expandedRow = descendingSize.headSet(descendingSize.higher(row.last()), true)), bounds, totalBytes), width)) {
            row = expandedRow;
        }

        return row;
    }

    private static NavigableSet<Double> areas(Set<Tree<FilesystemSummary>> summaries, Rect bounds, double totalBytes) {
        int totalArea = bounds.width() * bounds.height();
        return ImmutableSortedSet.copyOf(Iterables.transform(summaries, (summaryTree) -> totalArea * (summaryTree.getValue().getSubTreeBytes() / totalBytes)));
    }

    // See paper
    private double worst(NavigableSet<Double> areas, int w) {
        double s = 0;
        double min = areas.first();
        double max = min;
        for (double area : areas) {
            s += area;
            min = Math.min(min, area);
            max = Math.max(max, area);
        }

        return Math.max((w * w * max) / (s * s), (s * s) / (w * w * min));
    }
}
