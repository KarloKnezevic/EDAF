package com.knezevic.edaf.v3.web.config;

import com.knezevic.edaf.v3.persistence.jdbc.SchemaInitializer;
import com.knezevic.edaf.v3.persistence.query.JdbcRunRepository;
import com.knezevic.edaf.v3.persistence.query.RunRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Wires read repository and ensures schema exists on app startup.
 */
@Configuration
public class RepositoryConfig {

    @Bean
    public RunRepository runRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcRunRepository(dataSource);
    }
}
