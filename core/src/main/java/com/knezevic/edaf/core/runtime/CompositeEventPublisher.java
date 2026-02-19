package com.knezevic.edaf.core.runtime;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Fans out events to multiple {@link EventPublisher} instances.
 * <p>
 * This is the central event bus: algorithm → composite → [metrics, persistence, dashboard, console].
 * Thread-safe via {@link CopyOnWriteArrayList}.
 * </p>
 */
public final class CompositeEventPublisher implements EventPublisher {

    private final List<EventPublisher> delegates;

    public CompositeEventPublisher(List<EventPublisher> delegates) {
        this.delegates = new CopyOnWriteArrayList<>(delegates);
    }

    public CompositeEventPublisher() {
        this.delegates = new CopyOnWriteArrayList<>();
    }

    @Override
    public void publish(Object event) {
        for (EventPublisher delegate : delegates) {
            delegate.publish(event);
        }
    }

    public void addPublisher(EventPublisher publisher) {
        delegates.add(publisher);
    }

    public int size() {
        return delegates.size();
    }
}
