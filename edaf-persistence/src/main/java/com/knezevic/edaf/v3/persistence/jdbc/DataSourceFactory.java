package com.knezevic.edaf.v3.persistence.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Creates pooled DataSource instances for JDBC sinks and queries.
 */
public final class DataSourceFactory {

    private DataSourceFactory() {
        // utility class
    }

    public static DataSource create(String url, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user == null ? "" : user);
        config.setPassword(password == null ? "" : password);
        config.setMaximumPoolSize(5);
        config.setPoolName("edaf-v3-pool");
        return new HikariDataSource(config);
    }
}
