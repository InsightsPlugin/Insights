package net.frankheijden.insights.builders;

import net.frankheijden.insights.entities.ScanOptions;
import net.frankheijden.insights.events.ScanCompleteEvent;
import net.frankheijden.insights.tasks.LoadChunksTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The scanner class, used for scanning blocks and entities.
 */
public class Scanner {

    private final ScanOptions scanOptions;

    /**
     * Constructs a new scanner with options.
     * @param scanOptions The options to use when scanning
     */
    private Scanner(ScanOptions scanOptions) {
        this.scanOptions = scanOptions;
    }

    /**
     * Constructs a new scanner with options.
     * @param scanOptions The options to use when scanning
     * @return The newly created Scanner
     */
    public static Scanner create(ScanOptions scanOptions) {
        return new Scanner(scanOptions);
    }

    /**
     * Method used to retrieve a CompletableFuture with the result.
     * If you want to run the task synchronously, call .get() on the CompletableFuture.
     * If you want asynchronously, listen for the .whenComplete() method.
     * @return The ScanCompleteEvent in a CompletableFuture.
     */
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
