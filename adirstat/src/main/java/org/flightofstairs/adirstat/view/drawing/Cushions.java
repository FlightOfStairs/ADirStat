package org.flightofstairs.adirstat.view.drawing;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import com.google.common.base.Stopwatch;
import lombok.SneakyThrows;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.view.DisplayNode;
import org.flightofstairs.adirstat.view.colouring.Colouring;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Implementation of "Cushion Treemaps: Visualization of Hierarchical Information"
 * See http://www.win.tue.nl/~vanwijk/ctm.pdf
 */
public final class Cushions {

    public static final double F = 0.75;

    // Rendering constants... I wonder what these do?
    private static final double
            Ia = 40,
            Is = 215,
            Lx = 0.09759,
            Ly = 0.19518,
            Lz = 0.9759;

    public static final int MIN_DIMENSION = 1;

    @SneakyThrows
    public static Bitmap draw(@Nonnull Tree<DisplayNode> node, int width, int height) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        ExecutorService executorService = Executors.newCachedThreadPool();

        for (Runnable renderer : ctm(node, 0.5, Surface.create(), bitmap, true)) {
            executorService.submit(renderer);
        }
        executorService.shutdown();
        executorService.awaitTermination(1, MINUTES);

        Log.d(Cushions.class.getSimpleName(), "Rendered cushion treemap in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");

        return bitmap;
    }

    public static Iterable<Runnable> ctm(Tree<DisplayNode> node, double h, Surface s, Bitmap bitmap, boolean isRoot) {
        Rect bounds = node.getValue().getBounds();

        if (min(bounds.width(), bounds.height()) < MIN_DIMENSION) return emptyList();

        Surface newSurface = isRoot ? s : s.addCushion(bounds, h);

        if (node.getChildren().isEmpty()) return newArrayList(renderCushion(newSurface, bitmap, node.getValue()));
        else return concat(transform(node.getChildren(), child -> ctm(child, h * F, newSurface, bitmap, false)));
    }

    public static Runnable renderCushion(Surface s, Bitmap bitmap, DisplayNode displayNode) {
        return () -> {
            Rect bounds = displayNode.getBounds();

            double[] intensities = calculateIntensities(s, bounds);
            int[] pixels = calculatePixels(Colouring.getColour(displayNode.getFile()), intensities);

            bitmap.setPixels(pixels, 0, bounds.width(), bounds.left, bounds.top, bounds.width(), bounds.height());
        };
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

    private Cushions() { }
}
