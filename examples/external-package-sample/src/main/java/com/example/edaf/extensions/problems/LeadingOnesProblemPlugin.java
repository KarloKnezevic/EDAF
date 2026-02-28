/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.example.edaf.extensions.problems;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * ServiceLoader plugin registration for custom external Leading-Ones problem.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class LeadingOnesProblemPlugin implements ProblemPlugin<BitString> {

    @Override
    public String type() {
        return "leading-ones-ext";
    }

    @Override
    public String description() {
        return "External sample problem: maximize prefix of ones in bitstring";
    }

    @Override
    public LeadingOnesProblem create(Map<String, Object> params) {
        return new LeadingOnesProblem();
    }
}
