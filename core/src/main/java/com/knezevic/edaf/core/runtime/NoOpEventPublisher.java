package com.knezevic.edaf.core.runtime;

/**
 * A no-op event publisher that silently discards all events.
 * Used as a default when no event publishing is configured.
 */
public final class NoOpEventPublisher implements EventPublisher {
    @Override
    public void publish(Object event) {
        // intentionally empty
    }
}
