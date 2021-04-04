package dev.frankheijden.insights.concurrent;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.frankheijden.insights.api.InsightsPlugin;
import dev.frankheijden.insights.api.concurrent.ContainerExecutor;
import dev.frankheijden.insights.api.concurrent.containers.RunnableContainer;
import dev.frankheijden.insights.api.concurrent.containers.SupplierContainer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class ContainerExecutorService implements ContainerExecutor {

    private final ExecutorService executorService;

    private ContainerExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Constructs a new containerexecutor with given amount of worker threads.
     */
    public static ContainerExecutor newExecutor(int nThreads) {
        return new ContainerExecutorService(Executors.newFixedThreadPool(
                nThreads,
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
        return CompletableFuture.supplyAsync(container, executorService);
    }

    @Override
    public CompletableFuture<Void> submit(RunnableContainer container) {
        return CompletableFuture.runAsync(container, executorService);
    }

    @Override
    public void shutdown() {
        executorService.shutdownNow();
    }
}
