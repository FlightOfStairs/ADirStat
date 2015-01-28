package org.flightofstairs.adirstat;

// Like a Function<X, Void>
public interface Sink<X> {
    void apply(X input);
}
