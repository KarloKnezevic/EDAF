package com.knezevic.edaf.v3.cli.commands;

import com.knezevic.edaf.v3.cli.logging.LoggingConfigurator;
import com.knezevic.edaf.v3.cli.ui.Verbosity;
import com.knezevic.edaf.v3.core.config.ConfigDocumentType;
import com.knezevic.edaf.v3.core.config.ConfigLoader;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Config command group for validation and migration.
 */
@Command(
        name = "config",
        mixinStandardHelpOptions = true,
        description = "Config tools",
        subcommands = {
                ConfigCommand.ValidateCommand.class
        }
)
public final class ConfigCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use subcommand: validate");
    }

    /**
     * Validates one config file.
     */
    @Command(name = "validate", mixinStandardHelpOptions = true, description = "Validate config file")
    public static final class ValidateCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "Config path")
        private Path config;

        @Override
        public Integer call() {
            LoggingConfigurator.apply(Verbosity.NORMAL);
            ConfigLoader loader = new ConfigLoader();
            ConfigDocumentType type = loader.detectType(config);
            if (type == ConfigDocumentType.BATCH) {
                var batch = loader.loadBatch(config);
                System.out.println("Batch configuration is valid (experiments=" + batch.getExperiments().size() + ")");
                return 0;
            }

            var experiment = loader.load(config);
            System.out.println("Configuration is valid (schema=" + experiment.config().getSchema() + ")");
            for (String warning : experiment.warnings()) {
                System.out.println("[WARN] " + warning);
            }
            return 0;
        }
    }
}
