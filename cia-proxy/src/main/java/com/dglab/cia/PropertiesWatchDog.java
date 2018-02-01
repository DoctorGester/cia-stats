package com.dglab.cia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author doc
 */
public class PropertiesWatchDog implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(PropertiesWatchDog.class);

    private final Path file;
    private Consumer<Path> callback;

    public PropertiesWatchDog(final Path file, final Consumer<Path> callback) {
        this.file = file;
        this.callback = callback;
    }

    public void start() {
        new Thread(this).start();
    }

    private void waitUntilTheFileIsFound() {
        int logCounter = 0;

        while (!Files.exists(file)) {
            if (logCounter++ % 5 == 0) {
                log.warn("{} doesn't exist, can't start watching!", file);
            }

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                log.error("Interrupted", e);
            }
        }

        if (logCounter > 0) {
            log.info("{} was not initially present but then started existing, firing callback", file);
            callback.accept(file);
        }
    }

    @Override
    public void run() {
        waitUntilTheFileIsFound();

        log.info("Started watching file {}", file);

        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            file.toRealPath(LinkOption.NOFOLLOW_LINKS).getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                try {
                    final WatchKey watchKey = watchService.take();
                    for (WatchEvent<?> event: watchKey.pollEvents()) {
                        //we only register "ENTRY_MODIFY" so the context is always a Path.
                        final Path changed = (Path) event.context();
                        if (changed.equals(file)) {
                            log.info("{} changed, firing callback", file);
                            callback.accept(changed);
                        }
                    }

                    boolean valid = watchKey.reset();
                    if (!valid) {
                        log.error("Watch key is not valid anymore");
                        throw new IllegalStateException();
                    }
                } catch (InterruptedException e) {
                    log.error("Watchdog interrupted", e);
                }
            }
        } catch (IOException e) {
            log.error("Exception while registering watchdog", e);
        }
    }
}
