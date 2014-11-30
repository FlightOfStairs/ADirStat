package org.flightofstairs.adirstat.view;

import android.graphics.Rect;
import lombok.AllArgsConstructor;
import lombok.Value;

import javax.annotation.Nonnull;
import java.io.File;

@Value
@AllArgsConstructor(suppressConstructorProperties = true) // because android
public class DisplayNode implements Comparable<DisplayNode> {
    @Nonnull private File file;
    @Nonnull private Rect bounds;

    @Override
    public int compareTo(@Nonnull DisplayNode other) {
        return file.compareTo(other.file);
    }
}
