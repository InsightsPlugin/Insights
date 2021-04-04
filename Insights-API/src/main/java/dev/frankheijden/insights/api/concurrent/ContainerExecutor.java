package dev.frankheijden.insights.api.concurrent;

import dev.frankheijden.insights.api.concurrent.containers.RunnableContainer;
import dev.frankheijden.insights.api.concurrent.containers.SupplierContainer;
import java.util.concurrent.CompletableFuture;

public interface ContainerExecutor {

    <T> CompletableFuture<T> submit(SupplierContainer<T> container);

    CompletableFuture<Void> submit(RunnableContainer container);

    void shutdown();
}
