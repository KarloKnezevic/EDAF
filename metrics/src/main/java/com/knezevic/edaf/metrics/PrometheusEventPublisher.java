package com.knezevic.edaf.metrics;

import com.knezevic.edaf.core.runtime.EventPublisher;
import com.knezevic.edaf.core.runtime.AlgorithmStarted;
import com.knezevic.edaf.core.runtime.AlgorithmTerminated;
import com.knezevic.edaf.core.runtime.GenerationCompleted;
import com.knezevic.edaf.core.runtime.EvaluationCompleted;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public final class PrometheusEventPublisher implements EventPublisher {
    private final PrometheusMeterRegistry registry;
    private final EventPublisher delegate;
    private final Map<String, Timer.Sample> running = new ConcurrentHashMap<>();
    private final Map<String, Long> lastGenStart = new ConcurrentHashMap<>();
    private final Map<String, AtomicReference<Double>> bestFitness = new ConcurrentHashMap<>();
    private final Map<String, AtomicReference<Double>> worstFitness = new ConcurrentHashMap<>();
    private final Map<String, AtomicReference<Double>> avgFitness = new ConcurrentHashMap<>();
    private final Map<String, AtomicReference<Double>> stdFitness = new ConcurrentHashMap<>();

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
        int retries = 3;
        long delay = 500; // ms
        IOException lastException = null;
        
        for (int i = 0; i < retries; i++) {
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
                return; // Success
            } catch (java.net.BindException e) {
                lastException = e;
                if (i < retries - 1) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while waiting for port", ie);
                    }
                    delay *= 2; // Exponential backoff
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to start HTTP server on port " + port, e);
            }
        }
        
        // All retries failed
        throw new RuntimeException("Failed to bind to port " + port + " after " + retries + " retries. Port may be in use.", lastException);
    }

    private void registerGauges(String algorithmId) {
        AtomicReference<Double> best = bestFitness.get(algorithmId);
        AtomicReference<Double> worst = worstFitness.get(algorithmId);
        AtomicReference<Double> avg = avgFitness.get(algorithmId);
        AtomicReference<Double> std = stdFitness.get(algorithmId);
        
        // Use gauge name without dots (Prometheus converts dots to underscores)
        // Micrometer automatically handles tag conversion
        Gauge.builder("edaf_fitness_best", () -> {
            Double value = best.get();
            return Double.isNaN(value) ? 0.0 : value;
        })
                .description("Best fitness value in current generation")
                .tag("algorithm", algorithmId)
                .register(registry);
        
        Gauge.builder("edaf_fitness_worst", () -> {
            Double value = worst.get();
            return Double.isNaN(value) ? 0.0 : value;
        })
                .description("Worst fitness value in current generation")
                .tag("algorithm", algorithmId)
                .register(registry);
        
        Gauge.builder("edaf_fitness_avg", () -> {
            Double value = avg.get();
            return Double.isNaN(value) ? 0.0 : value;
        })
                .description("Average fitness value in current generation")
                .tag("algorithm", algorithmId)
                .register(registry);
        
        Gauge.builder("edaf_fitness_std", () -> {
            Double value = std.get();
            return Double.isNaN(value) ? 0.0 : value;
        })
                .description("Standard deviation of fitness values in current generation")
                .tag("algorithm", algorithmId)
                .register(registry);
    }

    @Override
    public void publish(Object event) {
        if (event instanceof AlgorithmStarted s) {
            String id = s.getAlgorithmId();
            running.put(id, Timer.start(registry));
            registry.counter("edaf.algorithm.started", "algorithm", id).increment();
            lastGenStart.put(id, System.nanoTime());
            // Initialize fitness gauges
            bestFitness.put(id, new AtomicReference<>(Double.NaN));
            worstFitness.put(id, new AtomicReference<>(Double.NaN));
            avgFitness.put(id, new AtomicReference<>(Double.NaN));
            stdFitness.put(id, new AtomicReference<>(Double.NaN));
            registerGauges(id);
        } else if (event instanceof AlgorithmTerminated t) {
            String id = t.getAlgorithmId();
            Timer.Sample sample = running.remove(id);
            if (sample != null) {
                sample.stop(registry.timer("edaf.algorithm.duration", "algorithm", id));
            }
            registry.counter("edaf.algorithm.terminated", "algorithm", id).increment();
            lastGenStart.remove(id);
            bestFitness.remove(id);
            worstFitness.remove(id);
            avgFitness.remove(id);
            stdFitness.remove(id);
        } else if (event instanceof GenerationCompleted g) {
            registry.counter("edaf.generation.completed", "algorithm", g.getAlgorithmId()).increment();
            Long start = lastGenStart.get(g.getAlgorithmId());
            if (start != null) {
                long nanos = System.nanoTime() - start;
                registry.timer("edaf.generation.duration", "algorithm", g.getAlgorithmId())
                        .record(nanos, java.util.concurrent.TimeUnit.NANOSECONDS);
                lastGenStart.put(g.getAlgorithmId(), System.nanoTime());
            }
            // Fitness statistics
            if (g.hasStatistics()) {
                String id = g.getAlgorithmId();
                AtomicReference<Double> bestRef = bestFitness.get(id);
                AtomicReference<Double> worstRef = worstFitness.get(id);
                AtomicReference<Double> avgRef = avgFitness.get(id);
                AtomicReference<Double> stdRef = stdFitness.get(id);
                
                if (bestRef == null) {
                    // Initialize if not already done (shouldn't happen, but safety check)
                    bestFitness.put(id, new AtomicReference<>(g.getBestFitness()));
                    worstFitness.put(id, new AtomicReference<>(g.getWorstFitness()));
                    avgFitness.put(id, new AtomicReference<>(g.getAvgFitness()));
                    stdFitness.put(id, new AtomicReference<>(g.getStdFitness()));
                    registerGauges(id);
                } else {
                    bestRef.set(g.getBestFitness());
                    worstRef.set(g.getWorstFitness());
                    avgRef.set(g.getAvgFitness());
                    stdRef.set(g.getStdFitness());
                }
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


