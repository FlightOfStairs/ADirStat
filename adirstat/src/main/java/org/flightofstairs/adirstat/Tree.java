package org.flightofstairs.adirstat;

import lombok.AllArgsConstructor;
import lombok.Value;

import javax.annotation.Nonnull;
import java.util.SortedSet;

@Value
@AllArgsConstructor(suppressConstructorProperties = true) // because android
public class Tree<T extends Comparable<T>> implements Comparable<Tree<T>> {
    @Nonnull private T value;
    @Nonnull private SortedSet<Tree<T>> children;

    @Override
    public int compareTo(@Nonnull Tree<T> another) {
        return value.compareTo(another.value);
    }
}
