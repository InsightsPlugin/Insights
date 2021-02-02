package dev.frankheijden.insights.concurrent;

import dev.frankheijden.insights.api.concurrent.ContainerExecutor;
import dev.frankheijden.insights.api.concurrent.containers.RunnableContainer;
import dev.frankheijden.insights.api.concurrent.containers.SupplierContainer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContainerExecutorService implements ContainerExecutor {

    private final ExecutorService executorService;

    private ContainerExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public static ContainerExecutor newExecutor(int nThreads) {
        return new ContainerExecutorService(Executors.newFixedThreadPool(nThreads));
    }

    @Override
    public <T> CompletableFuture<T> submit(SupplierContainer<T> container) {
        return CompletableFuture.supplyAsync(container, executorService);
    }

    @Override
    public CompletableFuture<Void> submit(RunnableContainer container) {
        return CompletableFuture.runAsync(container, executorService);
    }
}
