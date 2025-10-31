package com.knezevic.edaf.metrics;

import com.knezevic.edaf.core.runtime.EventPublisher;
import com.knezevic.edaf.core.runtime.AlgorithmStarted;
import com.knezevic.edaf.core.runtime.AlgorithmTerminated;
import com.knezevic.edaf.core.runtime.GenerationCompleted;
import com.knezevic.edaf.core.runtime.EvaluationCompleted;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PrometheusEventPublisher implements EventPublisher {
    private final PrometheusMeterRegistry registry;
    private final EventPublisher delegate;
    private final Map<String, Timer.Sample> running = new ConcurrentHashMap<>();
    private final Map<String, Long> lastGenStart = new ConcurrentHashMap<>();

    public PrometheusEventPublisher() {
        this(new PrometheusMeterRegistry(PrometheusConfig.DEFAULT), null, resolvePort());
    }

    public PrometheusEventPublisher(PrometheusMeterRegistry registry, EventPublisher delegate, int port) {
        this.registry = registry;
        this.delegate = delegate;
        startServer(port);
    }

    private static int resolvePort() {
        try {
            String p = System.getProperty("edaf.metrics.prometheus.port", "9464");
            return Integer.parseInt(p);
        } catch (Exception e) {
            return 9464;
        }
    }

    private void startServer(int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/metrics", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    byte[] bytes = registry.scrape().getBytes();
                    exchange.getResponseHeaders().add("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
                    exchange.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(bytes);
                    }
                }
            });
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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


