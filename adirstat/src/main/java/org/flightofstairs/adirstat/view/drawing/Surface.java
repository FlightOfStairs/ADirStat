package org.flightofstairs.adirstat.view.drawing;

import android.graphics.Rect;

// Parabolic constants for cushion 'height'
class Surface {
    final float x1;
    final float x2;

    final float y1;
    final float y2;

    private Surface(float x1, float x2, float y1, float y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    static Surface create() {
        return new Surface(0, 0, 0, 0);
    }

    Surface addCushion(Rect bounds, float h) {
        return new Surface(
                x1 + 4 * h * ((bounds.right + bounds.left) / (float) (bounds.right - bounds.left)),
                x2 - 4 * h / (bounds.right - bounds.left),
                y1 + 4 * h * ((bounds.bottom + bounds.top) / (float) (bounds.bottom - bounds.top)),
                y2 - 4 * h / (bounds.bottom - bounds.top));
    }
}
