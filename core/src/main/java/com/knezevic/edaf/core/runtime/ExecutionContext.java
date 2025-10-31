package com.knezevic.edaf.core.runtime;

import java.util.concurrent.ExecutorService;

public final class ExecutionContext {
    private final RandomSource randomSource;
    private final ExecutorService executor;
    private final EventPublisher events;

    public ExecutionContext(RandomSource randomSource, ExecutorService executor, EventPublisher events) {
        this.randomSource = randomSource;
        this.executor = executor;
        this.events = events;
    }

    public RandomSource getRandomSource() { return randomSource; }
    public ExecutorService getExecutor() { return executor; }
    public EventPublisher getEvents() { return events; }
}


