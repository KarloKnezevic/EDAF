/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entrypoint for EDAF v3 dashboard.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
@SpringBootApplication
public class EdafWebApplication {

    /**
     * Executes main.
     *
     * @param args method arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(EdafWebApplication.class, args);
    }
}
