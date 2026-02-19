package com.knezevic.edaf.v3.core.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe event fan-out used as the observability backbone.
 */
public final class EventBus implements AutoCloseable {

    private final List<EventSink> sinks = new CopyOnWriteArrayList<>();

    /**
     * Registers a sink.
     */
    public void register(EventSink sink) {
        sinks.add(sink);
    }

    /**
     * Publishes an event to all registered sinks.
     */
    public void publish(RunEvent event) {
        for (EventSink sink : sinks) {
            sink.onEvent(event);
        }
    }

    /**
     * Returns number of currently registered sinks.
     */
    public int sinkCount() {
        return sinks.size();
    }

    @Override
    public void close() {
        for (EventSink sink : sinks) {
            try {
                sink.close();
            } catch (Exception ignored) {
                // sink shutdown errors are isolated to preserve full shutdown sequence
            }
        }
    }
}
