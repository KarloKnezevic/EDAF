package com.knezevic.edaf.v3.cli.commands;

import com.knezevic.edaf.v3.cli.logging.LoggingConfigurator;
import com.knezevic.edaf.v3.cli.ui.Verbosity;
import com.knezevic.edaf.v3.persistence.jdbc.DataSourceFactory;
import com.knezevic.edaf.v3.persistence.jdbc.SchemaInitializer;
import com.knezevic.edaf.v3.persistence.query.JdbcRunRepository;
import com.knezevic.edaf.v3.reporting.HtmlReportGenerator;
import com.knezevic.edaf.v3.reporting.LatexReportGenerator;
import com.knezevic.edaf.v3.reporting.ReportService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Generates reports for an already persisted run.
 */
@Command(name = "report", description = "Generate run report from database")
public final class ReportCommand implements Callable<Integer> {

    @Option(names = "--run-id", required = true, description = "Run id")
    private String runId;

    @Option(names = "--out", required = true, description = "Output directory")
    private Path out;

    @Option(names = "--db-url", defaultValue = "jdbc:sqlite:edaf-v3.db", description = "JDBC URL")
    private String dbUrl;

    @Option(names = "--db-user", defaultValue = "", description = "DB user")
    private String dbUser;

    @Option(names = "--db-password", defaultValue = "", description = "DB password")
    private String dbPassword;

    @Option(names = "--formats", defaultValue = "html", description = "Comma-separated formats: html,latex")
    private String formats;

    @Option(names = "--verbosity", description = "Override verbosity: quiet|normal|verbose|debug")
    private String verbosity;

    @Override
    public Integer call() {
        Verbosity effectiveVerbosity = verbosity != null && !verbosity.isBlank()
                ? Verbosity.from(verbosity)
                : Verbosity.NORMAL;
        LoggingConfigurator.apply(effectiveVerbosity);

        var ds = DataSourceFactory.create(dbUrl, dbUser, dbPassword);
        SchemaInitializer.initialize(ds);

        ReportService service = new ReportService(
                new JdbcRunRepository(ds),
                List.of(new HtmlReportGenerator(), new LatexReportGenerator())
        );

        List<String> requestedFormats = Arrays.stream(formats.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        var artifacts = service.generate(runId, out, requestedFormats);
        System.out.println("Generated reports: " + artifacts);
        return 0;
    }
}
