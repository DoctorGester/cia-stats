package com.dglab.cia;

import com.dglab.cia.json.util.ExpiringObject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

/**
 * Created by shoujo on 2/2/2017.
 */
public class RemoteAsyncExpiringObject<T> extends ExpiringObject<T> {
    public RemoteAsyncExpiringObject(GithubHelper helper, String path, FileProcessor<T> processor) {
        super(() -> processor.process(helper.requestFile(path)), ChronoUnit.HOURS, 1);
    }

    @Override
    protected void update() {
        if (object == null) {
            object = supplier.get();
        } else {
            CompletableFuture.supplyAsync(supplier).thenAccept(o -> object = o);
        }

        lastUpdated = Instant.now();
    }
}
