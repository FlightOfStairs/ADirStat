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

    public Optional<Tree<T>> descendWhile(Predicate<T> predicate) {
        if (!predicate.apply(value)) return Optional.absent();

        for (Tree<T> child : children) {
            Optional<Tree<T>> childSearch = child.descendWhile(predicate);
            if (childSearch.isPresent()) return childSearch;
        }

        return Optional.of(this);
    }
}
