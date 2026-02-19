package com.knezevic.edaf.v3.persistence.sink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.core.events.IterationCompletedEvent;
import com.knezevic.edaf.v3.core.events.RunEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * CSV sink that writes one line per iteration metric snapshot.
 */
public final class CsvMetricsSink implements EventSink {

    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper();
    private boolean headerWritten;

    public CsvMetricsSink(Path file) {
        this.file = file;
    }

    @Override
    public synchronized void onEvent(RunEvent event) {
        if (!(event instanceof IterationCompletedEvent iteration)) {
            return;
        }

        try {
            Files.createDirectories(file.getParent() == null ? Path.of(".") : file.getParent());
            if (!headerWritten) {
                if (!Files.exists(file) || Files.size(file) == 0L) {
                    Files.writeString(file,
                            "timestamp,run_id,iteration,evaluations,best_fitness,mean_fitness,std_fitness,metrics_json,diagnostics_json\n",
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }
                headerWritten = true;
            }
            String line = String.join(",",
                    quote(iteration.timestamp().toString()),
                    quote(iteration.runId()),
                    String.valueOf(iteration.iteration()),
                    String.valueOf(iteration.evaluations()),
                    String.valueOf(iteration.bestFitness()),
                    String.valueOf(iteration.meanFitness()),
                    String.valueOf(iteration.stdFitness()),
                    quote(mapper.writeValueAsString(iteration.metrics())),
                    quote(mapper.writeValueAsString(iteration.diagnostics().numeric()))
            ) + "\n";
            Files.writeString(file, line, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed writing CSV metrics sink", e);
        }
    }

    private static String quote(String value) {
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
