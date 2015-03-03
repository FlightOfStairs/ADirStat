package org.flightofstairs.adirstat.view.drawing;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import lombok.SneakyThrows;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.view.DisplayNode;
import org.flightofstairs.adirstat.view.colouring.Colouring;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static java.lang.Math.min;
import static java.util.Collections.emptyList;

/**
 * Implementation of "Cushion Treemaps: Visualization of Hierarchical Information"
 * See http://www.win.tue.nl/~vanwijk/ctm.pdf
 */
public final class Cushions {

    public static final float F = 0.85f;

    // Rendering constants... I wonder what these do?
    private static final float
            Ia = 40f,
            Is = 215f,
            Lx = 0.09759f,
            Ly = 0.19518f,
            Lz = 0.9759f;

    public static final int MIN_DIMENSION = 1;
    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    @SneakyThrows
    public static Bitmap draw(@Nonnull Tree<DisplayNode> node, int width, int height) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        List<Future<Pair<Rect, int[]>>> futures = THREAD_POOL.invokeAll(ImmutableList.copyOf(ctm(node, 0.5f, Surface.create(), true)));

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (Future<Pair<Rect, int[]>> future : futures) {
            Rect bounds = future.get().first;
            bitmap.setPixels(future.get().second, 0, bounds.width(), bounds.left, bounds.top, bounds.width(), bounds.height());
        }

        Log.d(Cushions.class.getSimpleName(), "Rendered cushion treemap in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");

        return bitmap;
    }

    public static Iterable<Callable<Pair<Rect, int[]>>> ctm(Tree<DisplayNode> node, float h, Surface s, boolean isRoot) {
        Rect bounds = node.getValue().getBounds();

        if (min(bounds.width(), bounds.height()) < MIN_DIMENSION) return emptyList();

        Surface newSurface = isRoot ? s : s.addCushion(bounds, h);

        if (node.getChildren().isEmpty()) return ImmutableList.of(renderCushion(newSurface, node.getValue()));
        else return concat(transform(node.getChildren(), child -> ctm(child, h * F, newSurface, false)));
    }

    public static Callable<Pair<Rect, int[]>> renderCushion(Surface s, DisplayNode displayNode) {
        return () -> {
            float[] intensities = calculateIntensities(s, displayNode.getBounds());
            int[] pixels = calculatePixels(Colouring.getColour(displayNode.getFile()), intensities);
            return Pair.create(displayNode.getBounds(), pixels);
        };
    }

    private static int[] calculatePixels(int colour, float[] intensities) {
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

    private static float[] calculateIntensities(Surface s, Rect bounds) {
        if (bounds.isEmpty()) return new float[0];

        int width = bounds.width();
        int height = bounds.height();

        float[] nxSquared = new float[width];
        float[] nxScaled = new float[width];

        float[] nySquared = new float[height];
        float[] nyScaled = new float[height];

        for(int x = 0; x < width; x++) {
            float nx = -(2 * s.x2 * (x + bounds.left + 0.5f) + s.x1);
            nxSquared[x] = nx * nx;
            nxScaled[x] = nx * Lx;
        }

        for(int y = 0; y < height; y++) {
            float ny = -(2 * s.y2 * (y + bounds.top  + 0.5f) + s.y1);
            nySquared[y] = ny * ny;
            nyScaled[y] = ny * Ly;
        }

        float[] intensities = new float[width * height];

        for(int y = 0; y < height; y++) {
            int step = y * width;
            for(int x = 0; x < width; x++) {
                double cosa = (nxScaled[x] + nyScaled[y] + Lz) / Math.sqrt(nxSquared[x] + nySquared[y] + 1.0);

                intensities[(x + step)] = (float) (Ia + Math.max(0, Is * cosa));
            }
        }
        return intensities;
    }

    private Cushions() { }
}
