package org.flightofstairs.adirstat.view.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import com.google.common.base.Stopwatch;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.view.DisplayNode;
import org.flightofstairs.adirstat.view.colouring.Colouring;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;

/**
 * Implementation of "Cushion Treemaps: Visualization of Hierarchical Information"
 * See http://www.win.tue.nl/~vanwijk/ctm.pdf
 */
public class Cushions implements Drawing {

    // Rendering constants... I wonder what these do?
    private static final double
            Ia = 40,
            Is = 215,
            Lx = 0.09759,
            Ly = 0.19518,
            Lz = 0.9759;

    public static final int MIN_DIMENSION = 10;

    private final Colouring colouring;

    public Cushions(Colouring colouring) {
        this.colouring = colouring;
    }

    @Override
    public void draw(@Nonnull Tree<DisplayNode> node, @Nonnull Canvas canvas) {
        Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.RGB_565);

        Stopwatch stopwatch = Stopwatch.createStarted();
        makeCushionTreeMap(node, bitmap);
        Log.d(getClass().getSimpleName(), "Rendered cushion treemap in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");

        canvas.drawBitmap(bitmap, 0f, 0f, new Paint());
    }

    public void makeCushionTreeMap(Tree<DisplayNode> root, Bitmap bitmap) {
        ctm(root, null, 0.5, 0.75, new Surface(0, 0, 0, 0), bitmap);
    }

    public void ctm(Tree<DisplayNode> node, Tree<DisplayNode> parent, double h, double f, Surface s, Bitmap bitmap) {
        if (parent != null) {
            s = addRidge(s, h, node);
        }

        if (node.getChildren().isEmpty()) {
            renderCushion(s, bitmap, node.getValue());
        } else {
            for (Tree<DisplayNode> child : node.getChildren()) {
                ctm(child, node, h * f, f, s, bitmap);
            }
        }
    }

    public Surface addRidge(Surface s, double h, Tree<DisplayNode> node) {
        Rect bounds = node.getValue().getBounds();

        return new Surface(
                s.x1 + 4 * h * ((bounds.right + bounds.left) / (double) (bounds.right - bounds.left)),
                s.x2 - 4 * h / (bounds.right - bounds.left),
                s.y1 + 4 * h * ((bounds.bottom + bounds.top) / (double) (bounds.bottom - bounds.top)),
                s.y2 - 4 * h / (bounds.bottom - bounds.top));
    }

    public void renderCushion(Surface s, Bitmap bitmap, DisplayNode displayNode) {
        Rect bounds = displayNode.getBounds();

        if (min(bounds.width(), bounds.height()) < MIN_DIMENSION) return;

        float[] hsv = new float[3];
        final int colour = colouring.apply(displayNode.getFile());
        Color.colorToHSV(colour, hsv);

        int[] pixels = new int[bounds.width() * bounds.height()];
        Arrays.fill(pixels, Color.HSVToColor(new float[]{hsv[0], 1, 0}));

        for(int y = 0; y < bounds.height(); y++) {
            double ny = -(2 * s.y2 * (y + bounds.top + 0.5) + s.y1);
            for(int x = 0; x < bounds.width(); x++) {
                double nx = -(2 * s.x2 * (x + bounds.left + 0.5) + s.x1);
                double cosa = (nx * Lx + ny * Ly + Lz) / Math.sqrt(nx * nx + ny * ny + 1.0);

                float intensity = (float) (Ia + Math.max(0, Is * cosa));
                int shadedColour = Color.HSVToColor(new float[]{hsv[0], 1, intensity / 300f});

                pixels[x + y * bounds.width()] = shadedColour;
            }
        }

        bitmap.setPixels(pixels, 0, bounds.width(), bounds.left, bounds.top, bounds.width(), bounds.height());
    }

    // Parabolic constants for cushion 'height'
    private static class Surface {
        private final double x1;
        private final double x2;

        private final double y1;
        private final double y2;

        private Surface(double x1, double x2, double y1, double y2) {
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }
    }
}
