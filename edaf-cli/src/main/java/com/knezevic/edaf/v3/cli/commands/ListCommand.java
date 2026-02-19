package com.knezevic.edaf.v3.cli.commands;

import com.knezevic.edaf.v3.cli.logging.LoggingConfigurator;
import com.knezevic.edaf.v3.cli.ui.Verbosity;
import com.knezevic.edaf.v3.experiments.runner.ExperimentRunner;
import picocli.CommandLine.Command;

import java.util.Comparator;

/**
 * List command group for discoverable plugins.
 */
@Command(
        name = "list",
        description = "List available components",
        subcommands = {
                ListCommand.AlgorithmsCommand.class,
                ListCommand.ModelsCommand.class,
                ListCommand.ProblemsCommand.class
        }
)
public final class ListCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use subcommands: algorithms, models, problems");
    }

    /**
     * Lists available algorithm plugins.
     */
    @Command(name = "algorithms", description = "List algorithms")
    public static final class AlgorithmsCommand implements Runnable {
        @Override
        public void run() {
            LoggingConfigurator.apply(Verbosity.NORMAL);
            ExperimentRunner runner = new ExperimentRunner();
            runner.listAlgorithms().stream()
                    .sorted(Comparator.comparing(plugin -> plugin.type().toLowerCase()))
                    .forEach(plugin -> System.out.printf("%-24s %s%n", plugin.type(), plugin.description()));
        }
    }

    /**
     * Lists available model plugins.
     */
    @Command(name = "models", description = "List models")
    public static final class ModelsCommand implements Runnable {
        @Override
        public void run() {
            LoggingConfigurator.apply(Verbosity.NORMAL);
            ExperimentRunner runner = new ExperimentRunner();
            runner.listModels().stream()
                    .sorted(Comparator.comparing(plugin -> plugin.type().toLowerCase()))
                    .forEach(plugin -> System.out.printf("%-24s %s%n", plugin.type(), plugin.description()));
        }
    }

    /**
     * Lists available problem plugins.
     */
    @Command(name = "problems", description = "List problems")
    public static final class ProblemsCommand implements Runnable {
        @Override
        public void run() {
            LoggingConfigurator.apply(Verbosity.NORMAL);
            ExperimentRunner runner = new ExperimentRunner();
            runner.listProblems().stream()
                    .sorted(Comparator.comparing(plugin -> plugin.type().toLowerCase()))
                    .forEach(plugin -> System.out.printf("%-24s %s%n", plugin.type(), plugin.description()));
        }
    }
}
