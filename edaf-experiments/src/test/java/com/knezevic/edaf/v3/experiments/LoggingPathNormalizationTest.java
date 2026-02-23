package com.knezevic.edaf.v3.experiments;

import com.knezevic.edaf.v3.experiments.runner.ExperimentRunner;
import com.knezevic.edaf.v3.experiments.runner.RunExecution;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies log file sink path normalization to keep repository root clean.
 */
class LoggingPathNormalizationTest {

    @Test
    void rootRelativeFileLogIsRedirectedUnderOutputDirectory() throws Exception {
        Path outDir = Files.createTempDirectory("edaf-v3-log-normalization");
        var config = TestConfigFactory.baseConfig("log-normalization-run", outDir);
        config.getStopping().setMaxIterations(5);

        // Keep persistence lightweight for the test and explicitly enable rotating file sink.
        config.getPersistence().setSinks(List.of("csv"));
        config.getLogging().setModes(List.of("file"));
        config.getLogging().setLogFile("./edaf-v3.log");

        ExperimentRunner runner = new ExperimentRunner();
        RunExecution execution = runner.run(config, List.of());

        String logArtifact = execution.artifacts().get("log");
        assertNotNull(logArtifact, "log artifact path should be present");

        Path resolvedLog = Path.of(logArtifact).toAbsolutePath().normalize();
        Path expectedBase = outDir.resolve("logs").toAbsolutePath().normalize();
        assertTrue(resolvedLog.startsWith(expectedBase),
                "log path should be under outputDirectory/logs but was: " + resolvedLog);
        assertTrue(Files.exists(resolvedLog), "rotating file sink should create the log file");
    }
}
