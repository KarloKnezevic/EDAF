/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.web.config;

import com.knezevic.edaf.v3.persistence.jdbc.DataSourceFactory;
import com.knezevic.edaf.v3.persistence.jdbc.SchemaInitializer;
import com.knezevic.edaf.v3.persistence.query.JdbcRunRepository;
import com.knezevic.edaf.v3.persistence.query.RunRepository;
import com.knezevic.edaf.v3.persistence.query.coco.CocoRepository;
import com.knezevic.edaf.v3.persistence.query.coco.JdbcCocoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Wires read repository and ensures schema exists on app startup.
 */
@Configuration
public class RepositoryConfig {

    @Bean
    public DataSource dataSource(
            @Value("${spring.datasource.url:jdbc:sqlite:edaf-v3.db}") String url,
            @Value("${spring.datasource.username:}") String username,
            @Value("${spring.datasource.password:}") String password
    ) {
        return DataSourceFactory.create(url, username, password);
    }

    @Bean
    public RunRepository runRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcRunRepository(dataSource);
    }

    @Bean
    public CocoRepository cocoRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcCocoRepository(dataSource);
    }
}
