/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entrypoint for EDAF v3 dashboard.
 */
@SpringBootApplication
public class EdafWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdafWebApplication.class, args);
    }
}
