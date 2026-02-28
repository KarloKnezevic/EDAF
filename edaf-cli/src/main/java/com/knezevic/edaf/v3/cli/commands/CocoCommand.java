/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.cli.commands;

import com.knezevic.edaf.v3.cli.logging.LoggingConfigurator;
import com.knezevic.edaf.v3.cli.ui.Verbosity;
import com.knezevic.edaf.v3.coco.runner.CocoCampaignRunner;
import com.knezevic.edaf.v3.core.runtime.ExecutionParallelism;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * COCO/BBOB command group for campaign execution and reference comparison workflows.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
@Command(
        name = "coco",
        mixinStandardHelpOptions = true,
        description = "COCO/BBOB campaign commands",
        subcommands = {
                CocoCommand.RunCampaignCommand.class,
                CocoCommand.ImportReferenceCommand.class,
                CocoCommand.ReportCampaignCommand.class
        }
)
/**
 * CocoCommand implementation in this module.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class CocoCommand implements Runnable {

    /**
     * Prints usage hint for COCO subcommands.
     *
     * @return no return value
     */
    @Override
    public void run() {
        System.out.println("Use subcommands: run, import-reference, report");
    }

    /**
     * Executes one COCO campaign YAML.
     */
    @Command(name = "run", description = "Run COCO/BBOB campaign from YAML")
    public static final class RunCampaignCommand implements Callable<Integer> {

        @Option(names = {"-c", "--config"}, required = true, description = "Path to COCO campaign YAML")
        private Path configPath;

        @Option(names = "--verbosity", description = "Override verbosity: quiet|normal|verbose|debug")
        private String verbosity;

        /**
         * Executes CLI command.
         *
         * @return command exit code
         */
        @Override
        public Integer call() {
            Verbosity effectiveVerbosity = verbosity != null && !verbosity.isBlank()
                    ? Verbosity.from(verbosity)
                    : Verbosity.NORMAL;
            LoggingConfigurator.apply(effectiveVerbosity);

            CocoCampaignRunner runner = new CocoCampaignRunner();
            System.out.println("COCO run-level parallelism (auto): " + ExecutionParallelism.suggestedRunParallelism()
                    + " | visible CPUs: " + ExecutionParallelism.availableProcessors());
            var result = runner.run(configPath, List.of());

            System.out.println("COCO campaign completed: " + result.campaignId());
            System.out.println("Trials: " + result.executedTrials() + " total, " + result.successfulTrials() + " reached target");
            System.out.println("HTML report: " + result.htmlReport());
            if (!result.artifacts().isEmpty()) {
                System.out.println("Artifacts: " + result.artifacts());
            }
            return 0;
        }
    }

    /**
     * Imports reference ERT rows from CSV for campaign comparisons.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
     */
    @Command(name = "import-reference", description = "Import reference ERT CSV for COCO comparison")
    public static final class ImportReferenceCommand implements Callable<Integer> {

        @Option(names = "--csv", required = true, description = "Reference CSV path")
        private Path csvPath;

        @Option(names = "--suite", defaultValue = "bbob", description = "COCO suite name")
        private String suite;

        @Option(names = "--source-url",
                defaultValue = "https://numbbo.github.io/coco/",
                description = "Source URL documented in imported rows")
        private String sourceUrl;

        @Option(names = "--db-url", defaultValue = "jdbc:sqlite:edaf-v3.db", description = "JDBC URL")
        private String dbUrl;

        @Option(names = "--db-user", defaultValue = "", description = "DB user")
        private String dbUser;

        @Option(names = "--db-password", defaultValue = "", description = "DB password")
        private String dbPassword;

        /**
         * Executes CLI command.
         *
         * @return command exit code
         */
        @Override
        public Integer call() {
            LoggingConfigurator.apply(Verbosity.NORMAL);
            CocoCampaignRunner runner = new CocoCampaignRunner();
            int imported = runner.importReference(csvPath, dbUrl, dbUser, dbPassword, suite, sourceUrl);
            System.out.println("Imported reference rows: " + imported);
            return 0;
        }
    }

    /**
     * Rebuilds one campaign HTML report from persisted DB state.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 *
     */
    @Command(name = "report", description = "Generate COCO campaign HTML report from database")
    public static final class ReportCampaignCommand implements Callable<Integer> {

        @Option(names = "--campaign-id", required = true, description = "Campaign id")
        private String campaignId;

        @Option(names = "--out", required = true, description = "Output directory for HTML report")
        private Path outDir;

        @Option(names = "--db-url", defaultValue = "jdbc:sqlite:edaf-v3.db", description = "JDBC URL")
        private String dbUrl;

        @Option(names = "--db-user", defaultValue = "", description = "DB user")
        private String dbUser;

        @Option(names = "--db-password", defaultValue = "", description = "DB password")
        private String dbPassword;

        /**
         * Executes CLI command.
         *
         * @return command exit code
         */
        @Override
        public Integer call() {
            LoggingConfigurator.apply(Verbosity.NORMAL);
            CocoCampaignRunner runner = new CocoCampaignRunner();
            Path report = runner.generateReport(campaignId, dbUrl, dbUser, dbPassword, outDir);
            System.out.println("Generated COCO campaign report: " + report);
            return 0;
        }
    }
}
