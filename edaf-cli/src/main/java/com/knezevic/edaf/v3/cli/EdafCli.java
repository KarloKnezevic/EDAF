/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.cli;

import com.knezevic.edaf.v3.cli.commands.BatchCommand;
import com.knezevic.edaf.v3.cli.commands.CocoCommand;
import com.knezevic.edaf.v3.cli.commands.ConfigCommand;
import com.knezevic.edaf.v3.cli.commands.ListCommand;
import com.knezevic.edaf.v3.cli.commands.ReportCommand;
import com.knezevic.edaf.v3.cli.commands.ResumeCommand;
import com.knezevic.edaf.v3.cli.commands.RunCommand;
import com.knezevic.edaf.v3.cli.logging.LoggingConfigurator;
import com.knezevic.edaf.v3.cli.ui.Verbosity;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * EDAF v3 CLI entrypoint.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
@Command(
        name = "edaf",
        mixinStandardHelpOptions = true,
        version = "EDAF v3",
        description = "Research-grade Estimation of Distribution Algorithms Framework",
        subcommands = {
                RunCommand.class,
                BatchCommand.class,
                ResumeCommand.class,
                ReportCommand.class,
                ConfigCommand.class,
                ListCommand.class,
                CocoCommand.class
        }
)
/**
 * EdafCli implementation in this module.
 *
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class EdafCli implements Runnable {

    /**
     * Starts CLI command parsing and execution.
     *
     * @param args CLI arguments
     * @return no return value
     */
    public static void main(String[] args) {
        LoggingConfigurator.apply(Verbosity.NORMAL);
        AnsiConsole.systemInstall();
        try {
            int exit = new CommandLine(new EdafCli()).execute(args);
            System.exit(exit);
        } finally {
            AnsiConsole.systemUninstall();
        }
    }

    /**
     * Executes root command behavior when no subcommand is selected.
     *
     * @return no return value
     */
    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }
}
