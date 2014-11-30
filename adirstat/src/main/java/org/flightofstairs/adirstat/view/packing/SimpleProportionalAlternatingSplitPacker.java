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

import javax.annotation.Nonnull;

import static java.lang.Math.max;
import static java.lang.Math.round;

public class SimpleProportionalAlternatingSplitPacker implements Packer {
    public SimpleProportionalAlternatingSplitPacker(Scaling scaling) {
        this.scaling = scaling;
    }

    private final Scaling scaling;

    @Override
    public Tree<DisplayNode> pack(@Nonnull Tree<FilesystemSummary> summaryTree, @Nonnull Rect bounds) {
        return transformTree(summaryTree, bounds, bounds.width() < bounds.height() ? Split.VERTICAL : Split.HORIZONTAL);
    }

    private Tree<DisplayNode> transformTree(Tree<FilesystemSummary> summaryTree, Rect bounds, Split split) {
        ImmutableSortedSet.Builder<Tree<DisplayNode>> children = ImmutableSortedSet.naturalOrder();

        double priorFraction = 0;

        for (Tree<FilesystemSummary> child : summaryTree.getChildren()) {
            //noinspection ConstantConditions
            double fraction = max(0, scaling.apply((double) child.getValue().getSubTreeBytes()) / scaling.apply((double) summaryTree.getValue().getSubTreeBytes()));

            Log.v(getClass().getSimpleName(), fraction + "");

            children.add(transformTree(child, newBounds(bounds, split, fraction, priorFraction), split.invert()));

            priorFraction += fraction;
        }

        return new Tree<>(new DisplayNode(summaryTree.getValue().getPath(), bounds), children.build());
    }

    private static Rect newBounds(Rect bounds, Split split, double fraction, double priorFraction) {
        if (split == Split.HORIZONTAL) {
            double left = bounds.left + bounds.width() * priorFraction;
            double right =  left + round(bounds.width() * fraction);

            return new Rect((int) round(left), bounds.top, (int) round(right), bounds.bottom);
        } else {
            double top = bounds.top + bounds.height() * priorFraction;
            double bottom =  top + round(bounds.height() * fraction);

            return new Rect(bounds.left, (int) round(top), bounds.right, (int) round(bottom));
        }
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

    private enum Split {
        VERTICAL, HORIZONTAL;

        public Split invert() { return values()[(this.ordinal() + 1) % 2]; }
    }
}
