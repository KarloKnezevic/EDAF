package com.knezevic.edaf.dashboard;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javalin HTTP server serving the dashboard static frontend and SSE event stream.
 */
public class DashboardServer {

    private static final Logger log = LoggerFactory.getLogger(DashboardServer.class);

    private final int port;
    private final DashboardEventPublisher eventPublisher;
    private Javalin app;

    public DashboardServer(int port, DashboardEventPublisher eventPublisher) {
        this.port = port;
        this.eventPublisher = eventPublisher;
    }

    public void start() {
        app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.showJavalinBanner = false;
        });

        // SSE endpoint for real-time events
        app.sse("/sse/events", client -> {
            client.keepAlive();
            eventPublisher.addClient(client);
        });

        // Health check
        app.get("/api/health", ctx -> ctx.json(java.util.Map.of("status", "ok")));

        app.start(port);
        log.info("Dashboard started on http://localhost:{}", port);
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }

    public int getPort() {
        return port;
    }
}
