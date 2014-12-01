package org.flightofstairs.adirstat.view;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import com.google.common.base.Optional;
import com.squareup.otto.Bus;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.model.FilesystemSummary;
import org.flightofstairs.adirstat.view.drawing.Drawing;
import org.flightofstairs.adirstat.view.packing.Packing;

import javax.annotation.Nonnull;

public class TreeMap extends Drawable {
    private final Bus bus;
    private final Tree<FilesystemSummary> summaryTree;
    private final Packing packing;
    private final Drawing drawing;

    private Optional<Tree<DisplayNode>> root = Optional.absent();

    public TreeMap(@Nonnull Bus bus, @Nonnull Tree<FilesystemSummary> summaryTree, @Nonnull Packing packing, @Nonnull Drawing drawing) {
        this.bus = bus;
        this.summaryTree = summaryTree;
        this.packing = packing;
        this.drawing = drawing;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!root.isPresent()) {
            root = Optional.of(packing.pack(summaryTree, canvas.getClipBounds()));
            bus.post(root.get());
        }
        drawing.draw(root.get(), canvas);
    }

    @Override
    public int getOpacity() {return 0;}

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(ColorFilter cf) {}
}
