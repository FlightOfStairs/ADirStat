package org.flightofstairs.adirstat.view;

import android.graphics.Rect;

import com.google.common.collect.ComparisonChain;

import java.io.File;

import javax.annotation.Nonnull;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class DisplayNode implements Comparable<DisplayNode> {
    @Nonnull private File file;
    @Nonnull private Rect bounds;

    @Override
    public int compareTo(@Nonnull DisplayNode other) {
        //noinspection SuspiciousNameCombination
        return ComparisonChain.start()
                .compare(file, other.file)
                .compare(bounds.left, other.bounds.left)
                .compare(bounds.top, other.bounds.top)
                .compare(bounds.right, other.bounds.right)
                .compare(bounds.bottom, other.bounds.bottom)
                .result();
    }
}
