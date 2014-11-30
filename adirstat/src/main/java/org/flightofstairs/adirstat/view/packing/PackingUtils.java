package org.flightofstairs.adirstat.view.packing;

import android.graphics.Rect;

import static java.lang.Math.round;

public class PackingUtils {
    public enum Split {
        VERTICAL, HORIZONTAL;

        public Split invert() { return values()[(this.ordinal() + 1) % 2]; }

        public static Split forBounds(Rect bounds) {
            return bounds.width() < bounds.height() ? VERTICAL : HORIZONTAL;
        }
    }

    public static Rect newBounds(Rect bounds, Split split, double fraction, double priorFraction) {
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

    private PackingUtils() { }
}
