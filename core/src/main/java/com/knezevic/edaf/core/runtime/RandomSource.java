package com.knezevic.edaf.core.runtime;

import java.util.random.RandomGenerator;

public interface RandomSource {

    RandomGenerator generator();

    RandomSource split();
}


