package org.flightofstairs.adirstat.view.drawing;

import android.graphics.Rect;

// Parabolic constants for cushion 'height'
class Surface {
    final double x1;
    final double x2;

    final double y1;
    final double y2;

    private Surface(double x1, double x2, double y1, double y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

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
