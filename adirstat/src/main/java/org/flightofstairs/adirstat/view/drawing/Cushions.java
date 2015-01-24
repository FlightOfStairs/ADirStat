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

/**
 * Implementation of "Cushion Treemaps: Visualization of Hierarchical Information"
 * See http://www.win.tue.nl/~vanwijk/ctm.pdf
 */
public class Cushions implements Drawing {

    private static final int DIR_X = 0;
    private static final int DIR_Y = 1;

    private static final int D1 = 0;
    private static final int D2 = 1;

    private static final int BOUND_MIN = 0;
    private static final int BOUND_MAX = 1;

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
        double r[][] = new double[2][2];
        r[DIR_X][BOUND_MIN] = 0.0;
        r[DIR_X][BOUND_MAX] = bitmap.getWidth() + 0.0;
        r[DIR_Y][BOUND_MIN] = 0.0;
        r[DIR_Y][BOUND_MAX] = bitmap.getHeight() + 0.0;

        double[][] s = new double[2][2];
        s[DIR_X][D1] = 0.0;
        s[DIR_X][D2] = 0.0;
        s[DIR_X][D1] = 0.0;
        s[DIR_X][D2] = 0.0;

        ctm(root, null, r, 0.5, 0.75, DIR_X, s, bitmap);
    }

    public void ctm(Tree<DisplayNode> node, Tree<DisplayNode> parent, double[][] r, double h, double f, Integer d, double[][] s, Bitmap bitmap) {
        if (parent != null) {
            addRidge(d, r, s, h);
        }

        if (node.getChildren().isEmpty()) {
            renderCushion(r, s, bitmap, node.getValue());
        } else {
            d = d == DIR_X ? DIR_Y : DIR_X;

            double area = area(node.getValue().getBounds());

            double w = (r[d][BOUND_MAX] - r[d][BOUND_MIN]) / area;

            for (Tree<DisplayNode> child : node.getChildren()) {
                r[d][BOUND_MAX] = r[d][BOUND_MIN] + w * area(child.getValue().getBounds());
                ctm(child, node, r, h * f, f, d, s, bitmap);
                r[d][BOUND_MIN] = r[d][BOUND_MAX];
            }
        }
    }

    public void addRidge(Integer d, double[][] r, double[][] s, double h) {
        double x1 = r[d][BOUND_MIN];
        double x2 = r[d][BOUND_MAX];
        if ((x2 - x1) < 0.000001) return;

        s[d][D1] = s[d][D1] + 4 * h * ((x2 + x1) / (x2 - x1));
        s[d][D2] = s[d][D2] - 4 * h / (x2 - x1);
    }

    // This is a /very/ naive implementation of the algorithm.
    // Refactoring and optimisation comes later.
    public void renderCushion(double[][] r, double[][] s, Bitmap bitmap, DisplayNode displayNode) {
        double  Ia = 40,
                Is = 215,
                Lx = 0.09759,
                Ly = 0.19518,
                Lz = 0.9759;

        int xMin = (int) (r[DIR_X][BOUND_MIN] + 0.5);
        int xMax = (int) (r[DIR_X][BOUND_MAX] - 0.5);
        int width = Math.max(0, xMax - xMin);

        int yMin = (int) (r[DIR_Y][BOUND_MIN] + 0.5);
        int yMax = (int) (r[DIR_Y][BOUND_MAX] - 0.5);
        int height = Math.max(0, yMax - yMin);

        if (width  <= 0 || height <= 0) return;

        float[] hsv = new float[3];
        final int colour = colouring.apply(displayNode.getFile());
        Color.colorToHSV(colour, hsv);

        int[] pixels = new int[width * height];
        Arrays.fill(pixels, Color.HSVToColor(new float[]{hsv[0], 1, 0}));

        for(int y = 0; y < height; y++) {
            double ny = -(2 * s[DIR_Y][D2] * (y + yMin + 0.5) + s[DIR_Y][D1]);
            for(int x = 0; x < width; x++) {
                double nx = -(2 * s[DIR_X][D2] * (x + xMin + 0.5) + s[DIR_X][D1]);
                double cosa = (nx * Lx + ny * Ly + Lz) / Math.sqrt(nx * nx + ny * ny + 1.0);

                float intensity = (float) (Ia + Math.max(0, Is * cosa));
                int shadedColour = Color.HSVToColor(new float[]{hsv[0], 1, intensity / 300f});

                pixels[x + y * width] = shadedColour;
            }
        }
        bitmap.setPixels(pixels, 0, width, xMin, yMin, width, height);
    }

    private static double area(Rect rect) {
        return rect.width() * rect.height();
    }
}
