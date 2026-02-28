/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.core.events;

/**
 * Consumer for run events used by persistence, console, and web adapters.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
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
