package org.flightofstairs.adirstat.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.model.FilesystemSummary;

import static java.lang.Math.min;
import static java.lang.Math.round;

public class TreeMap extends Drawable {
    private enum Split { VERTICAL, HORIZONTAL }

    private final Tree<FilesystemSummary> root;

    public TreeMap(Tree<FilesystemSummary> root) {
        this.root = root;
    }

    @Override
    public void draw(Canvas canvas) {
        draw(root, canvas.getClipBounds(), canvas, canvas.getHeight() > canvas.getWidth() ? Split.VERTICAL : Split.HORIZONTAL);
    }

    private static void draw(Tree<FilesystemSummary> node, Rect bounds, Canvas canvas, Split split) {
        if (min(bounds.width(), bounds.height()) < 3 || bounds.width() * bounds.height() < 20) {
            Log.d(TreeMap.class.getSimpleName(), "Node too small to care about: " + node);
            return;
        }

        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(bounds, paint);

        double priorFraction = 0;

        for (Tree<FilesystemSummary> child : node.getChildren()) {
            double fraction = child.getValue().getSubTreeBytes() / (double) node.getValue().getSubTreeBytes();

            draw(child, newBounds(bounds, split, fraction, priorFraction), canvas, split == Split.VERTICAL ? Split.HORIZONTAL : Split.VERTICAL);

            priorFraction += fraction;
        }
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

    @Override
    public int getOpacity() {return 0;}

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(ColorFilter cf) {}
}
