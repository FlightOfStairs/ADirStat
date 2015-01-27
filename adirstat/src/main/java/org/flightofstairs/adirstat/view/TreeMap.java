package org.flightofstairs.adirstat.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import com.google.common.base.Optional;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.model.FilesystemSummary;
import org.flightofstairs.adirstat.view.drawing.Cushions;
import org.flightofstairs.adirstat.view.packing.SquarifiedPacking;

import javax.annotation.Nonnull;

public class TreeMap extends Drawable {
    private final Bus bus;
    private final Tree<FilesystemSummary> summaryTree;

    private Optional<Bitmap> image = Optional.absent();

    private Optional<DisplayNode> highlight = Optional.absent();

    public TreeMap(@Nonnull Bus bus, @Nonnull Tree<FilesystemSummary> summaryTree) {
        this.bus = bus;
        this.summaryTree = summaryTree;

        this.bus.register(this);
    }

    @Override
    public void draw(Canvas canvas) {
        if (!image.isPresent()) {
            Optional<Tree<DisplayNode>> root = Optional.of(SquarifiedPacking.pack(summaryTree, canvas.getClipBounds()));
            bus.post(root.get());

            image = Optional.of(Cushions.draw(root.get(), canvas.getWidth(), canvas.getHeight()));
        }

        canvas.drawBitmap(image.get(), 0, 0, new Paint());

        if (highlight.isPresent()) {
            Paint paint = new Paint();
            paint.setColor(Color.YELLOW);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);

            canvas.drawRect(highlight.get().getBounds(), paint);
        }
    }

    @Subscribe
    public void onSelectedNode(@Nonnull DisplayNode node) {
        highlight = Optional.of(node);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {return 0;}

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(ColorFilter cf) {}
}
