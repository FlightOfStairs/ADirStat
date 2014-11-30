package org.flightofstairs.adirstat.view;

import android.util.Pair;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import org.flightofstairs.adirstat.Tree;

public class TreeUtils {

    // I wish android had BinaryOperation... and lots of other things.
    // Would be much easier in haskell.
    public static <A extends Comparable<A>, B extends Comparable<B>, State> Tree<B> mapWithState(Tree<A> node, State state, Function<Pair<A, State>, B> function, Function<Pair<A, State>, State> stateFunction) {
        State newState = stateFunction.apply(Pair.create(node.getValue(), state));

        ImmutableSortedSet<Tree<B>> children = ImmutableSortedSet.copyOf(Iterables.transform(node.getChildren(), (child) -> mapWithState(child, newState, function, stateFunction)));

        return new Tree<>(function.apply(Pair.create(node.getValue(), state)), children);
    }
}
