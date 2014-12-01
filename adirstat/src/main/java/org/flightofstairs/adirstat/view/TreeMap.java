package org.flightofstairs.adirstat.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import com.google.common.base.Optional;
import com.squareup.otto.Bus;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.model.FilesystemSummary;
import org.flightofstairs.adirstat.view.colouring.Colouring;
import org.flightofstairs.adirstat.view.packing.Packing;

import javax.annotation.Nonnull;

public class TreeMap extends Drawable {
    private final Bus bus;
    private final Tree<FilesystemSummary> summaryTree;
    private final Packing packing;

    private Optional<Tree<DisplayNode>> root = Optional.absent();

    public TreeMap(@Nonnull Bus bus, @Nonnull Tree<FilesystemSummary> summaryTree, @Nonnull Packing packing) {
        this.bus = bus;
        this.summaryTree = summaryTree;
        this.packing = packing;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!root.isPresent()) {
            root = Optional.of(packing.pack(summaryTree, canvas.getClipBounds()));
            bus.post(root.get());
        }
        draw(root.get(), canvas);
    }

    private static void draw(Tree<DisplayNode> node, Canvas canvas) {
        int colour = Colouring.BASIC.apply(node.getValue().getFile());

        Paint paint = new Paint();
        paint.setColor(colour);
        canvas.drawRect(node.getValue().getBounds(), paint);

        float[] hsv = new float[3];
        Color.colorToHSV(colour, hsv);
        hsv[1] /= 2;
        paint.setColor(Color.HSVToColor(hsv));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(node.getValue().getBounds(), paint);

        for (Tree<DisplayNode> child : node.getChildren()) {
            draw(child, canvas);
        }
    }

    @Override
    public int getOpacity() {return 0;}

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(ColorFilter cf) {}
}
