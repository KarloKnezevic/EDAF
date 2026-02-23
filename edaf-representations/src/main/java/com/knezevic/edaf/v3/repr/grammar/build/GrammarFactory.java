package com.knezevic.edaf.v3.repr.grammar.build;

import com.knezevic.edaf.v3.repr.grammar.model.Grammar;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Entry-point factory for building grammar instances from plugin parameter maps.
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
     */
    public Grammar build(Map<String, Object> params) {
        GrammarConfig config = GrammarConfig.fromParams(params);
        return build(config);
    }

    /**
     * Builds grammar from canonical config object.
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
