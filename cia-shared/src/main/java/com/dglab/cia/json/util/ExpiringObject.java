package com.dglab.cia.json.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * User: kartemov
 * Date: 24.09.2016
 * Time: 0:50
 */
public class ExpiringObject<T> {
    protected T object;
    protected Supplier<T> supplier;
    private final ChronoUnit unit;
    private final long time;
    protected Instant lastUpdated;

    public ExpiringObject(Supplier<T> supplier, ChronoUnit unit, long time) {
        this.supplier = supplier;
        this.unit = unit;
        this.time = time;

        lastUpdated = Instant.now();
    }

    protected void update() {
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
