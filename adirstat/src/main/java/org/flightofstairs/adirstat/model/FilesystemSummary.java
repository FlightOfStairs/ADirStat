package org.flightofstairs.adirstat.model;

import com.google.common.collect.ComparisonChain;

import java.io.File;

import javax.annotation.Nonnull;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
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
