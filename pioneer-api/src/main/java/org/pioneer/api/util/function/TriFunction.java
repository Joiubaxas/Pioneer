package org.pioneer.api.util.function;

import java.util.Objects;
import java.util.function.Function;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */

/**
 * Represents a function that accepts three arguments and produces a result. This is the two-arity specialization of {@link Function}
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <O> the type of the third argument to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface TriFunction<T, U, O, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param t the first argument
     * @param u the second argument
     * @param o the third argument
     * @return the function result
     */
    R apply(T t, U u, O o);

    default <V> TriFunction<T, U, O, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (t, u, o) -> after.apply(this.apply(t, u, o));
    }
}
