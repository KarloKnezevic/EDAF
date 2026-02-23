package com.knezevic.edaf.v3.core.events;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies async sink ordering and flush-on-close behavior.
 */
class AsyncEventSinkTest {

    @Test
    void asyncSinkPreservesOrderAndFlushesOnClose() {
        RecordingSink delegate = new RecordingSink();
        AsyncEventSink sink = new AsyncEventSink(delegate, "async-test", 128);

        int total = 200;
        for (int i = 0; i < total; i++) {
            sink.onEvent(new TestEvent("run-1", "event-" + i));
        }
        sink.close();

        assertEquals(total, delegate.events.size());
        for (int i = 0; i < total; i++) {
            assertEquals("event-" + i, delegate.events.get(i).type());
        }
    }

    private static final class RecordingSink implements EventSink {
        private final List<RunEvent> events = new ArrayList<>();

        @Override
        public synchronized void onEvent(RunEvent event) {
            events.add(event);
        }
    }

    private record TestEvent(String runId, String type) implements RunEvent {
        @Override
        public Instant timestamp() {
            return Instant.now();
        }
    }
}
