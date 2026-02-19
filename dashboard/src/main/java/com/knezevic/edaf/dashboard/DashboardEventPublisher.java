package com.knezevic.edaf.dashboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knezevic.edaf.core.runtime.AlgorithmStarted;
import com.knezevic.edaf.core.runtime.AlgorithmTerminated;
import com.knezevic.edaf.core.runtime.EventPublisher;
import com.knezevic.edaf.core.runtime.GenerationCompleted;
import io.javalin.http.sse.SseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Pushes algorithm events as SSE JSON messages to connected dashboard clients.
 */
public class DashboardEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DashboardEventPublisher.class);
    private final Set<SseClient> clients = new CopyOnWriteArraySet<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public void addClient(SseClient client) {
        clients.add(client);
        client.onClose(() -> clients.remove(client));
    }

    @Override
    public void publish(Object event) {
        String json = toJson(event);
        if (json == null) return;

        for (SseClient client : clients) {
            try {
                client.sendEvent(eventType(event), json);
            } catch (Exception e) {
                clients.remove(client);
            }
        }
    }

    private String eventType(Object event) {
        if (event instanceof AlgorithmStarted) return "algorithmStarted";
        if (event instanceof GenerationCompleted) return "generationCompleted";
        if (event instanceof AlgorithmTerminated) return "algorithmTerminated";
        return "unknown";
    }

    private String toJson(Object event) {
        try {
            return switch (event) {
                case AlgorithmStarted s -> mapper.writeValueAsString(
                    Map.of("algorithmId", s.getAlgorithmId()));
                case GenerationCompleted g -> mapper.writeValueAsString(Map.of(
                    "algorithmId", g.getAlgorithmId(),
                    "generation", g.getGeneration(),
                    "bestFitness", g.getBestFitness(),
                    "worstFitness", g.getWorstFitness(),
                    "avgFitness", g.getAvgFitness(),
                    "stdFitness", g.getStdFitness()));
                case AlgorithmTerminated t -> mapper.writeValueAsString(Map.of(
                    "algorithmId", t.getAlgorithmId(),
                    "generation", t.getGeneration()));
                default -> null;
            };
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize event: {}", e.getMessage());
            return null;
        }
    }

    public int getClientCount() {
        return clients.size();
    }
}
