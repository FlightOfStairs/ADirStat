package org.flightofstairs.adirstat.view.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.view.DisplayNode;
import org.flightofstairs.adirstat.view.colouring.Colouring;

import javax.annotation.Nonnull;

public class SimpleFlatRects implements Drawing {

    private final Colouring colouring;

    public SimpleFlatRects(Colouring colouring) {
        this.colouring = colouring;
    }

    @Override
    public void draw(@Nonnull Tree<DisplayNode> node, @Nonnull Canvas canvas) {
        int colour = colouring.apply(node.getValue().getFile());

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
}
