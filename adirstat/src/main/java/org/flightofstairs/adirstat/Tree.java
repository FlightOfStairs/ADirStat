package org.flightofstairs.adirstat;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import lombok.AllArgsConstructor;
import lombok.Value;

import javax.annotation.Nonnull;
import java.util.SortedSet;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Value
@AllArgsConstructor(suppressConstructorProperties = true) // because android
public class Tree<T extends Comparable<T>> implements Comparable<Tree<T>> {
    @Nonnull private T value;
    @Nonnull private SortedSet<Tree<T>> children;

    @Override
    public int compareTo(@Nonnull Tree<T> another) {
        return value.compareTo(another.value);
    }

    @Nonnull
    public Optional<Tree<T>> descendWhile(@Nonnull Predicate<T> predicate) {
        if (!predicate.apply(value)) return Optional.absent();

        for (Tree<T> child : children) {
            Optional<Tree<T>> childSearch = child.descendWhile(predicate);
            if (childSearch.isPresent()) return childSearch;
        }

        return Optional.of(this);
    }

    @Nonnull
    public Optional<Tree<T>> stepDown(@Nonnull  Predicate<T> predicate) {
        if (!predicate.apply(value)) return Optional.absent();

        for (Tree<T> child : children) {
            if (predicate.apply(child.value)) return Optional.of(child);
        }

        return Optional.of(this);
    }
}
