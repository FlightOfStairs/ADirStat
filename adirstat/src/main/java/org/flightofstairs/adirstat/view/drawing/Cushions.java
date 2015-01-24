package org.flightofstairs.adirstat.view.drawing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
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
        Table<Dir, Bound, Double> r = TreeBasedTable.<Dir, Bound, Double>create();
        r.put(Dir.X, Bound.Min, 0.0);
        r.put(Dir.X, Bound.Max, bitmap.getWidth() + 0.0);
        r.put(Dir.Y, Bound.Min, 0.0);
        r.put(Dir.Y, Bound.Max, bitmap.getHeight() + 0.0);

        Table<Dir, Degree, Double> s = TreeBasedTable.<Dir, Degree, Double>create();
        s.put(Dir.X, Degree.D1, 0.0);
        s.put(Dir.X, Degree.D2, 0.0);
        s.put(Dir.Y, Degree.D1, 0.0);
        s.put(Dir.Y, Degree.D2, 0.0);

        ctm(root, null, r, 0.5, 0.75, Dir.X, s, bitmap);
    }

    public void ctm(Tree<DisplayNode> node, Tree<DisplayNode> parent, Table<Dir, Bound, Double> r, double h, double f, Dir d, Table<Dir, Degree, Double> s, Bitmap bitmap) {
        if (parent != null) {
            addRidge(d, r, s, h);
        }

        if (node.getChildren().isEmpty()) {
            renderCushion(r, s, bitmap, node.getValue());
        } else {
            d = d == Dir.X ? Dir.Y : Dir.X;

            double area = area(node.getValue().getBounds());

            double w = (r.get(d, Bound.Max) - r.get(d, Bound.Min)) / area;

            for (Tree<DisplayNode> child : node.getChildren()) {
                r.put(d, Bound.Max, r.get(d, Bound.Min) + w * area(child.getValue().getBounds()));
                ctm(child, node, r, h * f, f, d, s, bitmap);
                r.put(d, Bound.Min, r.get(d, Bound.Max));
            }
        }
    }

    public void addRidge(Dir d, Table<Dir, Bound, Double> r, Table<Dir, Degree, Double> s, double h) {
        double x1 = r.get(d, Bound.Min);
        double x2 = r.get(d, Bound.Max);
        if ((x2 - x1) < 0.000001) return;

        s.put(d, Degree.D1, s.get(d, Degree.D1) + 4 * h * ((x2 + x1) / (x2 - x1)));
        s.put(d, Degree.D2, s.get(d, Degree.D2) - 4 * h / (x2 - x1));
    }

    // This is a /very/ naive implementation of the algorithm.
    // Refactoring and optimisation comes later.
    public void renderCushion(Table<Dir, Bound, Double> r, Table<Dir, Degree, Double> s, Bitmap bitmap, DisplayNode displayNode) {
        double  Ia = 40,
                Is = 215,
                Lx = 0.09759,
                Ly = 0.19518,
                Lz = 0.9759;

        int xMin = (int) (r.get(Dir.X, Bound.Min) + 0.5);
        int xMax = (int) (r.get(Dir.X, Bound.Max) - 0.5);
        int width = Math.max(0, xMax - xMin);

        int yMin = (int) (r.get(Dir.Y, Bound.Min) + 0.5);
        int yMax = (int) (r.get(Dir.Y, Bound.Max) - 0.5);
        int height = Math.max(0, yMax - yMin);

        if (width  <= 0 || height <= 0) return;

        float[] hsv = new float[3];
        final int colour = colouring.apply(displayNode.getFile());
        Color.colorToHSV(colour, hsv);

        int[] pixels = new int[width * height];
        Arrays.fill(pixels, Color.HSVToColor(new float[]{hsv[0], 1, 0}));

        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                double nx = -(2 * s.get(Dir.X, Degree.D2) * (x + xMin + 0.5) + s.get(Dir.X, Degree.D1));
                double ny = -(2 * s.get(Dir.Y, Degree.D2) * (y + yMin + 0.5) + s.get(Dir.Y, Degree.D1));
                double cosa = (nx * Lx + ny * Ly + Lz) / Math.sqrt(nx * nx + ny * ny + 1.0);

                float intensity = (float) (Ia + Math.max(0, Is * cosa));
                int shadedColour = Color.HSVToColor(new float[]{hsv[0], 1, intensity / 300f});

                pixels[x + y * width] = shadedColour;
            }
        }
        bitmap.setPixels(pixels, 0, width, xMin, yMin, width, height);
    }

    private enum Dir {X, Y}
    private enum Bound {Min,Max}
    private enum Degree {D1, D2}
    //      type Rectangle = Table<Dir, Bound, Double>
    //      type Surface   = Table<Dir, Degree, Double>


    private static double area(Rect rect) {
        return rect.width() * rect.height();
    }
}
