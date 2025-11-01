package com.knezevic.edaf.core.runtime;

public final class ConsoleEventPublisher implements EventPublisher {
    @Override
    public void publish(Object event) {
        // Suppress console output - progress is shown via ProgressBar
        // Only log to file if needed
    }
}


