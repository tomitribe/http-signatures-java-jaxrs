/*
 * Tomitribe Confidential
 *
 * Copyright(c) Tomitribe Corporation. 2016
 *
 * The source code for this program is not published or otherwise divested
 * of its trade secrets, irrespective of what has been deposited with the
 * U.S. Copyright Office.
 */
package com.tomitribe.auth.signatures.jaxrs.internal.pool;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class LightPool<T> {
    private final Queue<T> pool = new ConcurrentLinkedQueue<>();

    public void consume(final Consumer<T> consumer) {
        T instance = pool.poll();
        if (instance == null) {
            instance = create();
        }
        try {
            consumer.accept(instance);
        } finally {
            release(instance);
            pool.add(instance); // auto adjusting (relative to the threading)
        }
    }

    protected void release(final T instance) {
        // no-op
    }

    protected abstract T create();

    public interface Consumer<T> {
        void accept(T instance);
    }
}
