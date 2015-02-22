package org.flightofstairs.adirstat.view.drawing;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Log;
import com.google.common.base.Stopwatch;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.view.DisplayNode;
import org.flightofstairs.adirstat.view.colouring.Colouring;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

/**
 * Implementation of "Cushion Treemaps: Visualization of Hierarchical Information"
 * See http://www.win.tue.nl/~vanwijk/ctm.pdf
 */
public final class Cushions {
    public static final double F = 0.85;

    public static final int MIN_DIMENSION = 1;

    private final RenderScript renderScript;
    private final ScriptC_cushion script;

    public Cushions(RenderScript renderScript) {
        this.renderScript = renderScript;
        this.script = new ScriptC_cushion(renderScript);
    }

    @SneakyThrows
    public Bitmap draw(@Nonnull Tree<DisplayNode> node, int width, int height) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        Iterable<Cushion> cushions = ctm(node, 0.5, Surface.create(), true);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (Cushion cushion : cushions) {
            Rect bounds = cushion.getNode().getBounds();
            bitmap.setPixels(renderCushion(cushion), 0, bounds.width(), bounds.left, bounds.top, bounds.width(), bounds.height());
        }

        Log.d(Cushions.class.getSimpleName(), "Rendered cushion treemap in " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");

        return bitmap;
    }

    public Iterable<Cushion> ctm(Tree<DisplayNode> node, double h, Surface s, boolean isRoot) {
        Rect bounds = node.getValue().getBounds();

        if (min(bounds.width(), bounds.height()) < MIN_DIMENSION) return emptyList();

        Surface newSurface = isRoot ? s : s.addCushion(bounds, h);

        if (node.getChildren().isEmpty()) return singleton(new Cushion(newSurface, node.getValue()));

        return concat(transform(node.getChildren(), child -> ctm(child, h * F, newSurface, false)));
    }

    public int[] renderCushion(Cushion cushion) {
        ScriptField_Surface.Item surface = new ScriptField_Surface.Item();
        surface.x1 = (float) cushion.getSurface().x1; surface.x2 = (float) cushion.getSurface().x2;
        surface.y1 = (float) cushion.getSurface().y1; surface.y2 = (float) cushion.getSurface().y2;

        script.set_s(surface);

        Rect bounds = cushion.getNode().getBounds();
        script.set_left(bounds.left);
        script.set_top(bounds.top);

        int colour = Colouring.getColour(cushion.getNode().getFile());
        script.set_r(Color.red(colour));
        script.set_g(Color.green(colour));
        script.set_b(Color.blue(colour));

        Type type = new Type.Builder(renderScript, Element.U32(renderScript)).setX(bounds.width()).setY(bounds.height()).create();
        Allocation out = Allocation.createTyped(renderScript, type);

        script.forEach_root(out);

        int[] pixels = new int[bounds.width() * bounds.height()];
        out.copyTo(pixels);

        return pixels;
    }

    @Value
    @AllArgsConstructor(suppressConstructorProperties = true) // because android
    private static class Cushion {
        Surface surface;
        DisplayNode node;
    }

    @Value
    @AllArgsConstructor(suppressConstructorProperties = true) // because android
    private static class Surface {
        double x1;
        double x2;

        double y1;
        double y2;

        static Surface create() {
            return new Surface(0, 0, 0, 0);
        }

        Surface addCushion(Rect bounds, double h) {
            return new Surface(
                    x1 + 4 * h * ((bounds.right + bounds.left) / (double) (bounds.right - bounds.left)),
                    x2 - 4 * h / (bounds.right - bounds.left),
                    y1 + 4 * h * ((bounds.bottom + bounds.top) / (double) (bounds.bottom - bounds.top)),
                    y2 - 4 * h / (bounds.bottom - bounds.top));
        }
    }
}
