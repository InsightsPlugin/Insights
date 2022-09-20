package dev.frankheijden.insights.concurrent;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ContainerExecutor;
import dev.frankheijden.insights.api.concurrent.containers.RunnableContainer;
import dev.frankheijden.insights.api.concurrent.containers.SupplierContainer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ContainerExecutorService implements ContainerExecutor {

    private final ThreadPoolExecutor executor;
    private final int timeoutMs;

    private ContainerExecutorService(ThreadPoolExecutor executor, int timeoutMs) {
        this.executor = executor;
        this.timeoutMs = timeoutMs;
    }

    /**
     * Constructs a new containerexecutor with given amount of worker threads.
     */
    public static ContainerExecutorService newExecutor(int nThreads, int timeoutMs) {
        return new ContainerExecutorService(
                new ThreadPoolExecutor(
                        nThreads,
                        nThreads,
                        0L, TimeUnit.MILLISECONDS,
                        new LinkedBlockingQueue<>(),
                        new ThreadFactoryBuilder()
                                .setNameFormat("Insights-worker-%d")
                                .setUncaughtExceptionHandler((t, e) -> InsightsPlugin.getInstance().getLogger().log(
                                        Level.SEVERE,
                                        String.format("[%s] Error occurred on worker thread:", t.getName()),
                                        e
                                ))
                                .build()
                ),
                timeoutMs
        );
    }

    @Override
    public <T> CompletableFuture<T> submit(SupplierContainer<T> container) {
        return CompletableFuture.supplyAsync(container, executor).orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<Void> submit(RunnableContainer container) {
        return CompletableFuture.runAsync(container, executor).orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public int getQueueSize() {
        return executor.getQueue().size();
    }

    public long getCompletedTaskCount() {
        return executor.getCompletedTaskCount();
    }

    @Override
    public void shutdown() {
        executor.shutdownNow();
    }
}
