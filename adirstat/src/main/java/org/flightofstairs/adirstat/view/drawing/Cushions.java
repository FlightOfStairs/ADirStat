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
import java.util.concurrent.TimeUnit;

import static java.lang.Math.min;

/**
 * Implementation of "Cushion Treemaps: Visualization of Hierarchical Information"
 * See http://www.win.tue.nl/~vanwijk/ctm.pdf
 */
public class Cushions implements Drawing {

    public static final double F = 0.75;

    // Rendering constants... I wonder what these do?
    private static final double
            Ia = 40,
            Is = 215,
            Lx = 0.09759,
            Ly = 0.19518,
            Lz = 0.9759;

    public static final int MIN_DIMENSION = 1;

    private final Colouring colouring;

    public Cushions(Colouring colouring) {
        this.colouring = colouring;
    }

    @Override
    public void draw(@Nonnull Tree<DisplayNode> node, @Nonnull Canvas canvas) {

        Stopwatch stopwatch = Stopwatch.createStarted();

        Bitmap bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        ctm(node, null, 0.5, new Surface(0, 0, 0, 0), bitmap);
        canvas.drawBitmap(bitmap, 0f, 0f, new Paint());

        Log.d(getClass().getSimpleName(), "Rendered cushion treemap in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
    }

    public void ctm(Tree<DisplayNode> node, Tree<DisplayNode> parent, double h, Surface s, Bitmap bitmap) {
        Rect bounds = node.getValue().getBounds();

        if (min(bounds.width(), bounds.height()) < MIN_DIMENSION) return;

        if (parent != null) {
            s = addRidge(s, h, bounds);
        }

        if (node.getChildren().isEmpty()) {
            renderCushion(s, bitmap, node.getValue());
        } else {
            for (Tree<DisplayNode> child : node.getChildren()) {
                ctm(child, node, h * F, s, bitmap);
            }
        }
    }

    public Surface addRidge(Surface s, double h, Rect bounds) {
        return new Surface(
                s.x1 + 4 * h * ((bounds.right + bounds.left) / (double) (bounds.right - bounds.left)),
                s.x2 - 4 * h / (bounds.right - bounds.left),
                s.y1 + 4 * h * ((bounds.bottom + bounds.top) / (double) (bounds.bottom - bounds.top)),
                s.y2 - 4 * h / (bounds.bottom - bounds.top));
    }

    public void renderCushion(Surface s, Bitmap bitmap, DisplayNode displayNode) {
        Rect bounds = displayNode.getBounds();

        double[] intensities = calculateIntensities(s, bounds);
        int[] pixels = calculatePixels(colouring.apply(displayNode.getFile()), intensities);

        bitmap.setPixels(pixels, 0, bounds.width(), bounds.left, bounds.top, bounds.width(), bounds.height());
    }

    private static int[] calculatePixels(int colour, double[] intensities) {
        // Color.HSVToColor() is much slower.

        final int a = 0xFF000000;
        final int r = Color.red(colour);
        final int g = Color.green(colour);
        final int b = Color.blue(colour);

        int[] pixels = new int[intensities.length];

        for (int pixel = 0; pixel < pixels.length; pixel++) {
            double intensity = intensities[pixel] / 256;

            pixels[pixel] = a
                    | (int) (r * intensity) << 16
                    | (int) (g * intensity) << 8
                    | (int) (b * intensity);
        }
        return pixels;
    }

    private static double[] calculateIntensities(Surface s, Rect bounds) {
        double[] intensities = new double[bounds.width() * bounds.height()];

        for(int y = 0; y < bounds.height(); y++) {
            double ny = -(2 * s.y2 * (y + bounds.top + 0.5) + s.y1);
            for(int x = 0; x < bounds.width(); x++) {
                double nx = -(2 * s.x2 * (x + bounds.left + 0.5) + s.x1);
                double cosa = (nx * Lx + ny * Ly + Lz) / Math.sqrt(nx * nx + ny * ny + 1.0);

                intensities[(x + y * bounds.width())] = (float) (Ia + Math.max(0, Is * cosa));
            }
        }
        return intensities;
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
