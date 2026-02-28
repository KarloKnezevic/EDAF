/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.example.edaf.extensions.problems;

import com.knezevic.edaf.v3.core.api.Fitness;
import com.knezevic.edaf.v3.core.api.ObjectiveSense;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.ScalarFitness;
import com.knezevic.edaf.v3.repr.types.BitString;

/**
 * Custom benchmark that maximizes number of consecutive ones from index 0.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class LeadingOnesProblem implements Problem<BitString> {

    @Override
    public String name() {
        return "leading-ones-ext";
    }

    @Override
    public ObjectiveSense objectiveSense() {
        return ObjectiveSense.MAXIMIZE;
    }

    @Override
    public Fitness evaluate(BitString genotype) {
        int[] bits = genotype.bits();
        int leading = 0;
        for (int bit : bits) {
            if (bit != 1) {
                break;
            }
            leading++;
        }
        return new ScalarFitness(leading);
    }
}
