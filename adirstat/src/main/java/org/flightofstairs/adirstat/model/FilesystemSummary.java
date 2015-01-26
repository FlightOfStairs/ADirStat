package org.flightofstairs.adirstat.model;

import com.google.common.collect.ComparisonChain;
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
    public int compareTo(@Nonnull FilesystemSummary other) {
        return ComparisonChain.start()
                .compare(path, other.path)
                .compare(subTreeBytes, other.subTreeBytes)
                .compare(subTreeCount, other.subTreeCount)
                .result();
    }
}
