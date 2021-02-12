package dev.frankheijden.insights.api.metrics;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.LongAdder;

public class IntegerMetric implements Callable<Integer> {

    private final LongAdder adder = new LongAdder();

    @Override
    public Integer call() {
        return Math.toIntExact(Math.min(adder.sumThenReset(), Integer.MAX_VALUE));
    }

    public void increment() {
        adder.increment();
    }
}
