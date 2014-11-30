package org.flightofstairs.adirstat.view.packing;

import android.graphics.Rect;

import javax.annotation.Nonnull;

import static com.google.common.base.Verify.verify;

public class PackingUtils {
    private static final double EPSILON = 0.00001;

    public enum Split {
        VERTICAL, HORIZONTAL;

        public Split invert() { return values()[(this.ordinal() + 1) % 2]; }

        public static Split forBounds(Rect bounds) {
            return bounds.width() < bounds.height() ? VERTICAL : HORIZONTAL;
        }
    }

    public static Rect newBounds(@Nonnull Rect bounds, double rowFraction, double priorFraction) {
        return newBounds(bounds, rowFraction, priorFraction, PackingUtils.Split.forBounds(bounds));
    }

    @Nonnull
    public static Rect newBounds(@Nonnull Rect bounds, double fraction, double priorFraction, Split split) {
        verify(fraction + priorFraction <= 1 + EPSILON, "fraction (%d) & prior fraction (%d) more than 1 + EPSILON ", fraction, priorFraction);

        Rect newBounds;

        if (split == Split.HORIZONTAL) {
            double left = bounds.left + bounds.width() * priorFraction;
            double right =  left + (bounds.width() * fraction);

            newBounds = new Rect((int) left, bounds.top, (int) right, bounds.bottom);
        } else {
            double top = bounds.top + bounds.height() * priorFraction;
            double bottom =  top + bounds.height() * fraction;

            newBounds = new Rect(bounds.left, (int) top, bounds.right, (int) bottom);
        }

        verify(newBounds.left >= bounds.left && newBounds.top >= bounds.top && newBounds.right <= bounds.right && newBounds.bottom <= bounds.bottom);

        return newBounds;
    }

    private PackingUtils() { }
}
