package com.knezevic.edaf.v3.persistence.sink;

import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.core.events.RunEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Composes multiple sinks into a single sink instance.
 */
public final class CompositeSink implements EventSink {

    private final List<EventSink> sinks = new CopyOnWriteArrayList<>();

    public CompositeSink(List<EventSink> sinks) {
        this.sinks.addAll(sinks);
    }

    public CompositeSink add(EventSink sink) {
        this.sinks.add(sink);
        return this;
    }

    @Override
    public void onEvent(RunEvent event) {
        for (EventSink sink : sinks) {
            sink.onEvent(event);
        }
    }

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
