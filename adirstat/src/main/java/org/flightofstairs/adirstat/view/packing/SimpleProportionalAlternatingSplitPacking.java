package org.flightofstairs.adirstat.view.packing;

import android.graphics.Rect;
import android.util.Log;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedSet;
import lombok.experimental.Delegate;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.model.FilesystemSummary;
import org.flightofstairs.adirstat.view.DisplayNode;
import org.flightofstairs.adirstat.view.packing.PackingUtils.Split;

import javax.annotation.Nonnull;

import static java.lang.Math.max;
import static org.flightofstairs.adirstat.view.packing.PackingUtils.newBounds;

public class SimpleProportionalAlternatingSplitPacking implements Packing {
    public SimpleProportionalAlternatingSplitPacking(Scaling scaling) {
        this.scaling = scaling;
    }

    private final Scaling scaling;

    @Nonnull
    @Override
    public Tree<DisplayNode> pack(@Nonnull Tree<FilesystemSummary> summaryTree, @Nonnull Rect bounds) {
        return pack(summaryTree, bounds, Split.forBounds(bounds));
    }

    private Tree<DisplayNode> pack(Tree<FilesystemSummary> summaryTree, Rect bounds, Split split) {
        ImmutableSortedSet.Builder<Tree<DisplayNode>> children = ImmutableSortedSet.naturalOrder();

        double priorFraction = 0;

        for (Tree<FilesystemSummary> child : summaryTree.getChildren()) {
            //noinspection ConstantConditions
            double fraction = max(0, scaling.apply((double) child.getValue().getSubTreeBytes()) / scaling.apply((double) summaryTree.getValue().getSubTreeBytes()));

            Log.v(getClass().getSimpleName(), fraction + "");

            children.add(pack(child, newBounds(bounds, split, fraction, priorFraction), split.invert()));

            priorFraction += fraction;
        }

        return new Tree<>(new DisplayNode(summaryTree.getValue().getPath(), bounds), children.build());
    }

    public enum Scaling implements Function<Double, Double> {
        LINEAR(Functions.<Double>identity()),
        LOG(Math::log);

        @Delegate
        private final Function<Double, Double> delegate;

        Scaling(Function<Double, Double> delegate) {
            this.delegate = delegate;
        }
    }
}
