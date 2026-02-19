package com.knezevic.edaf.v3.core.events;

/**
 * Consumer for run events used by persistence, console, and web adapters.
 */
public interface EventSink extends AutoCloseable {

    /**
     * Consumes one event.
     */
    void onEvent(RunEvent event);

    @Override
    default void close() {
        // default no-op
    }
}
