package com.knezevic.edaf.v3.core.rng;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serializable snapshot of all RNG streams used by a run.
 */
public record RngSnapshot(long masterSeed, Map<String, RngStreamState> streams) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public RngSnapshot {
        streams = Collections.unmodifiableMap(new LinkedHashMap<>(streams));
    }
}
