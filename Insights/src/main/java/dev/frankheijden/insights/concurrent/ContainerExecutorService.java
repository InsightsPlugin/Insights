package dev.frankheijden.insights.concurrent;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ContainerExecutor;
import dev.frankheijden.insights.api.concurrent.containers.RunnableContainer;
import dev.frankheijden.insights.api.concurrent.containers.SupplierContainer;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ContainerExecutorService implements ContainerExecutor {

    private final ThreadPoolExecutor executor;

    private ContainerExecutorService(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    /**
     * Constructs a new containerexecutor with given amount of worker threads.
     */
    public static ContainerExecutorService newExecutor(int nThreads) {
        return new ContainerExecutorService(new ThreadPoolExecutor(nThreads, nThreads,
                0L, TimeUnit.MILLISECONDS,
                new PriorityBlockingQueue<>(1024, Comparator.comparing(Runnable::hashCode)),
                new ThreadFactoryBuilder()
                        .setNameFormat("Insights-worker-%d")
                        .setUncaughtExceptionHandler((t, e) -> InsightsPlugin.getInstance().getLogger().log(
                                Level.SEVERE,
                                String.format("[%s] Error occurred on worker thread:", t.getName()),
                                e
                        )).build()
        ));
    }

    @Override
    public <T> CompletableFuture<T> submit(SupplierContainer<T> container) {
        return CompletableFuture.supplyAsync(container, executor);
    }

    @Override
    public CompletableFuture<Void> submit(RunnableContainer container) {
        return CompletableFuture.runAsync(container, executor);
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
