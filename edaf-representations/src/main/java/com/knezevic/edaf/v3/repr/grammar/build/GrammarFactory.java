/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.repr.grammar.build;

import com.knezevic.edaf.v3.repr.grammar.model.Grammar;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Entry-point factory for building grammar instances from plugin the input value maps.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class GrammarFactory {

    private final AutoGrammarBuilder autoBuilder;
    private final GrammarParser parser;

    /**
     * Creates grammar factory with default parser/builder dependencies.
     */
    public GrammarFactory() {
        this.autoBuilder = new AutoGrammarBuilder();
        this.parser = new GrammarParser();
    }

    /**
     * Builds grammar using {@code grammar.mode} from params.
     * @param params configuration the input value map
     * @return constructed grammar
     */
    public Grammar build(Map<String, Object> params) {
        GrammarConfig config = GrammarConfig.fromParams(params);
        return build(config);
    }

    /**
     * Builds grammar from canonical config object.
     * @param config grammar configuration
     * @return constructed grammar
     */
    public Grammar build(GrammarConfig config) {
        if ("auto".equals(config.mode())) {
            return autoBuilder.build(config);
        }
        Path file = resolveFile(config.file());
        return parser.parse(file, config);
    }

    private static Path resolveFile(String file) {
        if (file == null || file.isBlank()) {
            throw new IllegalArgumentException("grammar.file is required when grammar.mode=custom");
        }
        Path direct = Path.of(file);
        if (Files.exists(direct)) {
            return direct;
        }
        Path configsPath = Path.of("configs").resolve(file);
        if (Files.exists(configsPath)) {
            return configsPath;
        }
        throw new IllegalArgumentException("Custom grammar file not found: " + file);
    }
}
