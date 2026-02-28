/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.persistence.sink;

import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.core.events.RunEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Composes multiple sinks into a single sink instance.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class CompositeSink implements EventSink {

    private final List<EventSink> sinks = new CopyOnWriteArrayList<>();

    /**
     * Creates a new CompositeSink instance.
     *
     * @param sinks the sinks argument
     */
    public CompositeSink(List<EventSink> sinks) {
        this.sinks.addAll(sinks);
    }

    /**
     * Executes add.
     *
     * @param sink event sink
     * @return the add
     */
    public CompositeSink add(EventSink sink) {
        this.sinks.add(sink);
        return this;
    }

    /**
     * Executes on event.
     *
     * @param event run event payload
     */
    @Override
    public void onEvent(RunEvent event) {
        for (EventSink sink : sinks) {
            sink.onEvent(event);
        }
    }

    /**
     * Executes close.
     *
     */
    @Override
    public void close() {
        for (EventSink sink : sinks) {
            try {
                sink.close();
            } catch (Exception ignored) {
                // close all sinks even when one fails
            }
        }
    }
}
