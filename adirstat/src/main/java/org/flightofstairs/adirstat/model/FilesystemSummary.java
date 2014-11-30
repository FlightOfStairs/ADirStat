package org.flightofstairs.adirstat.model;

import lombok.AllArgsConstructor;
import lombok.Value;

import javax.annotation.Nonnull;
import java.io.File;

@Value
@AllArgsConstructor(suppressConstructorProperties = true) // needed because android :(
public class FilesystemSummary implements Comparable<FilesystemSummary> {
    private @Nonnull File path;
    private long subTreeBytes;
    private long subTreeCount;

    @Override
    public int compareTo(FilesystemSummary other) {
        return this.path.compareTo(other.path);
    }
}
