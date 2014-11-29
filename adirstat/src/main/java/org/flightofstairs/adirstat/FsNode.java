package org.flightofstairs.adirstat;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.util.SortedSet;

@Value
@AllArgsConstructor(suppressConstructorProperties = true) // needed because android :(
public class FsNode implements Comparable<FsNode> {
    @NonNull String name;
    @NonNull SortedSet<FsNode> children;
    long subTreeBytes;
    long subTreeCount;

    @Override
    public int compareTo(FsNode other) {
        return this.name.compareTo(other.name);
    }
}
