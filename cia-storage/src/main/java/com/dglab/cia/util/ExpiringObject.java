package com.dglab.cia.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

/**
 * User: kartemov
 * Date: 24.09.2016
 * Time: 0:50
 */
public class ExpiringObject<T> {
    private T object;
    private Supplier<T> supplier;
    private final ChronoUnit unit;
    private final long time;
    private Instant lastUpdated;

    public ExpiringObject(Supplier<T> supplier, ChronoUnit unit, long time) {
        this.supplier = supplier;
        this.unit = unit;
        this.time = time;

        lastUpdated = Instant.now();
    }

    private void update() {
        object = supplier.get();
        lastUpdated = Instant.now();
    }

    public synchronized T get() {
        Instant now = Instant.now();

        if (object == null || unit.between(this.lastUpdated, now) > time) {
            update();
        }

        return object;
    }
}
