package com.knezevic.edaf.core.runtime;

public final class ConsoleEventPublisher implements EventPublisher {
    @Override
    public void publish(Object event) {
        System.out.println(String.valueOf(event));
    }
}


