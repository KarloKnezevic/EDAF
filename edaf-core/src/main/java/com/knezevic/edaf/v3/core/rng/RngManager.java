package com.knezevic.edaf.v3.core.rng;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deterministic RNG stream manager.
 *
 * Streams are derived from a master seed and a component key to ensure independent and reproducible random flows.
 */
public final class RngManager {

    private final long masterSeed;
    private final Map<String, RngStream> streams = new ConcurrentHashMap<>();

    public RngManager(long masterSeed) {
        this.masterSeed = masterSeed;
    }

    /**
     * Returns (or lazily creates) a deterministic named stream.
     */
    public RngStream stream(String component) {
        Objects.requireNonNull(component, "component must not be null");
        return streams.computeIfAbsent(component, key -> new RngStream(key, new StatefulRandom(deriveSeed(masterSeed, key))));
    }

    /**
     * Captures complete RNG state for checkpoint/resume.
     */
    public RngSnapshot snapshot() {
        Map<String, RngStreamState> map = new LinkedHashMap<>();
        for (Map.Entry<String, RngStream> entry : streams.entrySet()) {
            map.put(entry.getKey(), entry.getValue().snapshot());
        }
        return new RngSnapshot(masterSeed, map);
    }

    /**
     * Restores stream states from checkpoint.
     */
    public void restore(RngSnapshot snapshot) {
        for (Map.Entry<String, RngStreamState> entry : snapshot.streams().entrySet()) {
            RngStream stream = stream(entry.getKey());
            stream.restore(entry.getValue());
        }
    }

    public long masterSeed() {
        return masterSeed;
    }

    /**
     * Stable seed derivation for component streams using SplitMix64 style mixing.
     */
    public static long deriveSeed(long masterSeed, String component) {
        long z = masterSeed ^ fnv1a64(component);
        z += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private static long fnv1a64(String value) {
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < value.length(); i++) {
            hash ^= value.charAt(i);
            hash *= 0x100000001b3L;
        }
        return hash;
    }
}
