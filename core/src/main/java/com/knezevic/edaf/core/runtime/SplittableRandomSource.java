package com.knezevic.edaf.core.runtime;

import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

public final class SplittableRandomSource implements RandomSource {
    private final SplittableRandom random;

    public SplittableRandomSource(long seed) {
        this.random = new SplittableRandom(seed);
    }

    private SplittableRandomSource(SplittableRandom random) {
        this.random = random;
    }

    @Override
    public RandomGenerator generator() {
        return random;
    }

    @Override
    public RandomSource split() {
        return new SplittableRandomSource(random.split());
    }
}


