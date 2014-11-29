package org.flightofstairs.adirstat.model;

import lombok.AllArgsConstructor;
import lombok.Value;

import javax.annotation.Nonnull;

@Value
@AllArgsConstructor(suppressConstructorProperties = true) // needed because android :(
public class FilesystemSummary implements Comparable<FilesystemSummary> {
    private @Nonnull String name;
    private long subTreeBytes;
    private long subTreeCount;

    @Override
    public int compareTo(FilesystemSummary other) {
        return this.name.compareTo(other.name);
    }
}
