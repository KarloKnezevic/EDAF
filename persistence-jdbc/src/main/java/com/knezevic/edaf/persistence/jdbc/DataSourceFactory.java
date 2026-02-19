package com.knezevic.edaf.persistence.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Creates a HikariCP connection pool from JDBC configuration.
 */
public final class DataSourceFactory {

    private DataSourceFactory() {}

    public static DataSource create(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        if (user != null) {
            config.setUsername(user);
        }
        if (password != null) {
            config.setPassword(password);
        }

        // SQLite doesn't support concurrent connections well
        if (url.contains("sqlite")) {
            config.setMaximumPoolSize(1);
        } else {
            config.setMaximumPoolSize(5);
        }

        config.setAutoCommit(true);
        return new HikariDataSource(config);
    }
}
