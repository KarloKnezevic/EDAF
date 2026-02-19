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
