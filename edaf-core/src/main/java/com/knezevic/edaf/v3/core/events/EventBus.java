/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe event fan-out used as the observability backbone.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class EventBus implements AutoCloseable {

    private final List<EventSink> sinks = new CopyOnWriteArrayList<>();

    /**
     * Registers a sink.
     * @param sink event sink
     */
    public void register(EventSink sink) {
        sinks.add(sink);
    }

    /**
     * Publishes an event to all registered sinks.
     * @param event event payload
     */
    public void publish(RunEvent event) {
        for (EventSink sink : sinks) {
            sink.onEvent(event);
        }
    }

    /**
     * Returns number of currently registered sinks.
     * @return the computed sink count
     */
    public int sinkCount() {
        return sinks.size();
    }

    /**
     * Executes close.
     *
     */
    @Override
    public void close() {
        RuntimeException failure = null;
        for (EventSink sink : sinks) {
            try {
                sink.close();
            } catch (Exception e) {
                if (failure == null) {
                    failure = new RuntimeException("One or more event sinks failed during shutdown", e);
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }
}
