package net.frankheijden.insights.builders;

import net.frankheijden.insights.entities.ScanOptions;
import net.frankheijden.insights.events.ScanCompleteEvent;
import net.frankheijden.insights.tasks.LoadChunksTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class Scanner {
    private final ScanOptions scanOptions;

    private Scanner(ScanOptions scanOptions) {
        this.scanOptions = scanOptions;
    }

    public static Scanner create(ScanOptions scanOptions) {
        return new Scanner(scanOptions);
    }

    public CompletableFuture<ScanCompleteEvent> scan() {
        return CompletableFuture.supplyAsync(() -> {
            final Object LOCK = new Object();

            AtomicReference<ScanCompleteEvent> event = new AtomicReference<>();
            scanOptions.setListener(ev -> {
                event.set(ev);

                synchronized (LOCK) {
                    LOCK.notify();
                }
            });

            LoadChunksTask task = new LoadChunksTask(scanOptions);
            task.start();

            synchronized (LOCK) {
                try {
                    LOCK.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            return event.get();
        });
    }
}
