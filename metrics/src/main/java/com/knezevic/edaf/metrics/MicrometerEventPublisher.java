package com.knezevic.edaf.metrics;

import com.knezevic.edaf.core.runtime.EventPublisher;
import com.knezevic.edaf.core.runtime.AlgorithmStarted;
import com.knezevic.edaf.core.runtime.AlgorithmTerminated;
import com.knezevic.edaf.core.runtime.GenerationCompleted;
import com.knezevic.edaf.core.runtime.EvaluationCompleted;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MicrometerEventPublisher implements EventPublisher {
    private final MeterRegistry registry;
    private final EventPublisher delegate;
    private final Map<String, Timer.Sample> running = new ConcurrentHashMap<>();
    private final Map<String, Long> lastGenStart = new ConcurrentHashMap<>();

    public MicrometerEventPublisher() {
        this(new SimpleMeterRegistry(), null);
    }

    public MicrometerEventPublisher(MeterRegistry registry, EventPublisher delegate) {
        this.registry = registry;
        this.delegate = delegate;
    }

    @Override
    public void publish(Object event) {
        if (event instanceof AlgorithmStarted s) {
            running.put(s.getAlgorithmId(), Timer.start(registry));
            registry.counter("edaf.algorithm.started", "algorithm", s.getAlgorithmId()).increment();
            lastGenStart.put(s.getAlgorithmId(), System.nanoTime());
        } else if (event instanceof AlgorithmTerminated t) {
            Timer.Sample sample = running.remove(t.getAlgorithmId());
            if (sample != null) {
                sample.stop(registry.timer("edaf.algorithm.duration", "algorithm", t.getAlgorithmId()));
            }
            registry.counter("edaf.algorithm.terminated", "algorithm", t.getAlgorithmId()).increment();
            lastGenStart.remove(t.getAlgorithmId());
        } else if (event instanceof GenerationCompleted g) {
            registry.counter("edaf.generation.completed", "algorithm", g.getAlgorithmId()).increment();
            Long start = lastGenStart.get(g.getAlgorithmId());
            if (start != null) {
                long nanos = System.nanoTime() - start;
                registry.timer("edaf.generation.duration", "algorithm", g.getAlgorithmId())
                        .record(nanos, java.util.concurrent.TimeUnit.NANOSECONDS);
                lastGenStart.put(g.getAlgorithmId(), System.nanoTime());
            }
        } else if (event instanceof EvaluationCompleted e) {
            registry.counter("edaf.evaluations.count", "algorithm", e.getAlgorithmId())
                    .increment(e.getEvaluatedCount());
            registry.timer("edaf.evaluation.duration", "algorithm", e.getAlgorithmId())
                    .record(e.getDurationNanos(), java.util.concurrent.TimeUnit.NANOSECONDS);
        }
        if (delegate != null) {
            delegate.publish(event);
        }
    }
}


