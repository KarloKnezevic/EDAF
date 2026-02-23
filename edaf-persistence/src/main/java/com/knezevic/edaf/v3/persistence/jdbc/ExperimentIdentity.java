package com.knezevic.edaf.v3.persistence.jdbc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * Canonical identity derived from config payload while ignoring per-run mutable fields.
 */
public record ExperimentIdentity(
        JsonNode normalizedConfig,
        String canonicalJson,
        String canonicalYaml,
        String configHash,
        String experimentId
) {

    private static final ObjectMapper CANONICAL_JSON = new ObjectMapper()
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    private static final ObjectMapper CANONICAL_YAML = new ObjectMapper(new YAMLFactory())
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    /**
     * Builds canonical experiment identity from one canonical run config JSON payload.
     */
    public static ExperimentIdentity fromCanonicalJson(String canonicalConfigJson) {
        try {
            JsonNode parsed = CANONICAL_JSON.readTree(canonicalConfigJson);
            JsonNode normalized = normalizeForExperimentFingerprint(parsed);
            String canonicalJson = CANONICAL_JSON.writeValueAsString(normalized);
            String canonicalYaml = CANONICAL_YAML.writeValueAsString(normalized);
            String hash = sha256(canonicalJson);
            return new ExperimentIdentity(normalized, canonicalJson, canonicalYaml, hash, hash);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid canonical JSON configuration", e);
        }
    }

    private static JsonNode normalizeForExperimentFingerprint(JsonNode parsed) {
        JsonNode deepCopy = parsed.deepCopy();
        if (!(deepCopy instanceof ObjectNode root)) {
            return deepCopy;
        }

        JsonNode runNode = root.path("run");
        if (runNode instanceof ObjectNode runObject) {
            runObject.remove("id");
            runObject.remove("masterSeed");
        }

        JsonNode loggingNode = root.path("logging");
        if (loggingNode instanceof ObjectNode loggingObject) {
            loggingObject.remove("jsonlFile");
            loggingObject.remove("logFile");
        }

        return root;
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                out.append(String.format(Locale.ROOT, "%02x", b));
            }
            return out.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed computing config hash", e);
        }
    }
}
