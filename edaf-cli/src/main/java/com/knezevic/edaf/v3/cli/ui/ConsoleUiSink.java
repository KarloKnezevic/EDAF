package com.knezevic.edaf.v3.cli.ui;

import com.knezevic.edaf.v3.core.events.EventSink;
import com.knezevic.edaf.v3.core.events.IterationCompletedEvent;
import com.knezevic.edaf.v3.core.events.RunCompletedEvent;
import com.knezevic.edaf.v3.core.events.RunEvent;
import com.knezevic.edaf.v3.core.events.RunStartedEvent;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import picocli.CommandLine;

/**
 * Rich console sink with color, progress bar, compact iteration lines, and run summary.
 */
public final class ConsoleUiSink implements EventSink {

    private final Verbosity verbosity;
    private final int maxIterations;
    private final int summaryEvery;
    private ProgressBar progressBar;

    public ConsoleUiSink(Verbosity verbosity, int maxIterations, int summaryEvery) {
        this.verbosity = verbosity;
        this.maxIterations = Math.max(1, maxIterations);
        this.summaryEvery = Math.max(1, summaryEvery);
    }

    @Override
    public void onEvent(RunEvent event) {
        if (event instanceof RunStartedEvent started) {
            printBanner(started);
            ProgressBarBuilder builder = new ProgressBarBuilder()
                    .setTaskName("Iterations")
                    .setInitialMax(maxIterations)
                    .setStyle(ProgressBarStyle.ASCII)
                    .setUnit(" it", 1)
                    .setUpdateIntervalMillis(120);
            progressBar = builder.build();
            return;
        }

        if (event instanceof IterationCompletedEvent iteration) {
            if (progressBar != null) {
                progressBar.stepTo(Math.min(maxIterations, iteration.iteration()));
                progressBar.setExtraMessage(String.format(
                        java.util.Locale.ROOT,
                        "best=%.6f mean=%.6f std=%.6f",
                        iteration.bestFitness(),
                        iteration.meanFitness(),
                        iteration.stdFitness()
                ));
            }

            if (verbosity == Verbosity.VERBOSE || verbosity == Verbosity.DEBUG) {
                printlnAnsi("@|cyan iter|@ " + iteration.iteration()
                        + " @|green best|@=" + format(iteration.bestFitness())
                        + " @|yellow mean|@=" + format(iteration.meanFitness())
                        + " @|magenta std|@=" + format(iteration.stdFitness())
                        + " evals=" + iteration.evaluations()
                        + " adaptive=" + iteration.adaptiveActions().size());
                if (!iteration.adaptiveActions().isEmpty()) {
                    for (var action : iteration.adaptiveActions()) {
                        printlnAnsi("  @|bold,fg(yellow)adaptive|@ trigger=" + action.trigger()
                                + " action=" + action.actionType()
                                + " reason=" + action.reason());
                    }
                }
            }

            if (verbosity != Verbosity.QUIET && iteration.iteration() % summaryEvery == 0) {
                printSummaryRow(iteration);
            }
            return;
        }

        if (event instanceof RunCompletedEvent completed) {
            if (progressBar != null) {
                progressBar.stepTo(maxIterations);
                progressBar.close();
                progressBar = null;
            }
            printFinalSummary(completed);
        }
    }

    @Override
    public void close() {
        if (progressBar != null) {
            progressBar.close();
            progressBar = null;
        }
    }

    private void printBanner(RunStartedEvent started) {
        if (verbosity == Verbosity.QUIET) {
            return;
        }
        printlnAnsi("@|bold,fg(cyan)==============================================|@");
        printlnAnsi("@|bold,fg(cyan) EDAF v3 RUN|@  " + started.runId());
        printlnAnsi("algorithm=" + started.algorithm() + "  model=" + started.model() + "  problem=" + started.problem());
        printlnAnsi("seed=" + started.masterSeed());
        printlnAnsi("@|bold,fg(cyan)==============================================|@");
    }

    private void printSummaryRow(IterationCompletedEvent iteration) {
        printlnAnsi(String.format(
                java.util.Locale.ROOT,
                "@|bold [iter %4d]|@  best=%10.6f  mean=%10.6f  std=%10.6f  evals=%8d",
                iteration.iteration(),
                iteration.bestFitness(),
                iteration.meanFitness(),
                iteration.stdFitness(),
                iteration.evaluations()
        ));
    }

    private void printFinalSummary(RunCompletedEvent completed) {
        if (verbosity == Verbosity.QUIET) {
            System.out.println(completed.bestFitness());
            return;
        }
        printlnAnsi("\n@|bold,fg(green)Run Completed|@ " + completed.runId());
        printlnAnsi("iterations=" + completed.iterations()
                + "  evaluations=" + completed.evaluations()
                + "  runtimeMs=" + completed.runtimeMillis());
        printlnAnsi("@|bold bestFitness|@=" + format(completed.bestFitness()));
        printlnAnsi("@|bold best|@ " + completed.bestSummary());
        if (!completed.artifacts().isEmpty()) {
            printlnAnsi("@|bold artifacts|@ " + completed.artifacts());
        }
    }

    private String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.6f", value);
    }

    private static void printlnAnsi(String message) {
        System.out.println(CommandLine.Help.Ansi.AUTO.string(message));
    }
}
