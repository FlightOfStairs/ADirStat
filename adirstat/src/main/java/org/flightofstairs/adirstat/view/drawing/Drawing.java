package org.flightofstairs.adirstat.view.drawing;

import android.graphics.Canvas;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.view.DisplayNode;

import javax.annotation.Nonnull;

public interface Drawing {
    void draw(@Nonnull Tree<DisplayNode> node, @Nonnull Canvas canvas);
}
