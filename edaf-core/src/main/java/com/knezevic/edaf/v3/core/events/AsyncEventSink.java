package com.knezevic.edaf.v3.core.events;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Asynchronous wrapper around one sink with bounded queue and single worker.
 *
 * <p>The wrapper preserves event order per sink, applies backpressure when the queue is full,
 * and guarantees flush-on-close semantics.</p>
 */
public final class AsyncEventSink implements EventSink {

    private static final Envelope POISON = new Envelope(null, true);

    private final EventSink delegate;
    private final BlockingQueue<Envelope> queue;
    private final Thread worker;
    private final Object lifecycleLock = new Object();

    private volatile RuntimeException workerFailure;
    private volatile boolean closing;

    /**
     * Creates async wrapper with explicit queue capacity.
     */
    public AsyncEventSink(EventSink delegate, String workerName, int queueCapacity) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        this.queue = new ArrayBlockingQueue<>(Math.max(64, queueCapacity));
        String threadName = (workerName == null || workerName.isBlank())
                ? "edaf-async-sink"
                : workerName;
        this.worker = new Thread(this::drainLoop, threadName);
        this.worker.setDaemon(true);
        this.worker.start();
    }

    @Override
    public void onEvent(RunEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        synchronized (lifecycleLock) {
            ensureHealthy();
            try {
                queue.put(new Envelope(event, false));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while enqueueing event for async sink", e);
            }
        }
    }

    @Override
    public void close() {
        synchronized (lifecycleLock) {
            if (closing) {
                return;
            }
            closing = true;
            try {
                queue.put(POISON);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while requesting async sink shutdown", e);
            }
        }

        try {
            worker.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for async sink worker shutdown", e);
        }

        RuntimeException closeFailure = null;
        try {
            delegate.close();
        } catch (Exception e) {
            closeFailure = new RuntimeException("Failed closing delegate sink", e);
        }

        if (workerFailure != null && closeFailure != null) {
            closeFailure.addSuppressed(workerFailure);
            throw closeFailure;
        }
        if (workerFailure != null) {
            throw workerFailure;
        }
        if (closeFailure != null) {
            throw closeFailure;
        }
    }

    private void ensureHealthy() {
        if (workerFailure != null) {
            throw workerFailure;
        }
        if (closing) {
            throw new IllegalStateException("AsyncEventSink is closing and cannot accept new events");
        }
    }

    private void drainLoop() {
        try {
            while (true) {
                Envelope envelope = queue.take();
                if (envelope.poison()) {
                    return;
                }
                delegate.onEvent(envelope.event());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            workerFailure = new RuntimeException("Async sink worker interrupted", e);
        } catch (RuntimeException e) {
            workerFailure = e;
        } catch (Exception e) {
            workerFailure = new RuntimeException("Async sink delegate failed", e);
        }
    }

    private record Envelope(RunEvent event, boolean poison) {
    }
}
