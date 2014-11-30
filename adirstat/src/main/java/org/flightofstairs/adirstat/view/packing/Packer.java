package org.flightofstairs.adirstat.view.packing;

import android.graphics.Rect;
import org.flightofstairs.adirstat.Tree;
import org.flightofstairs.adirstat.model.FilesystemSummary;
import org.flightofstairs.adirstat.view.DisplayNode;

import javax.annotation.Nonnull;

public interface Packer {
    Tree<DisplayNode> pack(@Nonnull Tree<FilesystemSummary> summaryTree, @Nonnull Rect bounds);
}
