package net.frankheijden.insights.api.builders;

import net.frankheijden.insights.Insights;
import net.frankheijden.insights.api.entities.ScanOptions;
import net.frankheijden.insights.api.events.ScanCompleteEvent;
import net.frankheijden.insights.tasks.LoadChunksTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class Scanner {
    private final Insights plugin;
    private final ScanOptions scanOptions;

    private Scanner(Insights plugin, ScanOptions scanOptions) {
        this.plugin = plugin;
        this.scanOptions = scanOptions;
    }

    public static Scanner create(ScanOptions scanOptions) {
        return new Scanner(Insights.getInstance(), scanOptions);
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

            LoadChunksTask task = new LoadChunksTask(plugin, scanOptions);
            task.start(System.currentTimeMillis());

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
