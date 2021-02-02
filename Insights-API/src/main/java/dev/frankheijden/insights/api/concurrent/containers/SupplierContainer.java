package dev.frankheijden.insights.api.concurrent.containers;

import java.util.function.Supplier;

public interface SupplierContainer<T> extends Supplier<T>, RunnableContainer {

}
