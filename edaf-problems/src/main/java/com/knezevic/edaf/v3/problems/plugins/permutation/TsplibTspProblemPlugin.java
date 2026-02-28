/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.permutation;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.permutation.TsplibTspProblem;
import com.knezevic.edaf.v3.problems.permutation.tsplib.TsplibInstance;
import com.knezevic.edaf.v3.problems.util.ProblemResourceLoader;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
 * Plugin factory for TSPLIB TSP instances.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class TsplibTspProblemPlugin implements ProblemPlugin<PermutationVector> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "tsplib-tsp";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "TSP from TSPLIB NODE_COORD_SECTION instance";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public TsplibTspProblem create(Map<String, Object> params) {
        String instance = Params.str(params, "instance", "classpath:tsplib/berlin52.tsp");
        TsplibInstance parsed = TsplibInstance.parse(ProblemResourceLoader.readText(instance));
        return new TsplibTspProblem(parsed.name(), parsed.coordinates());
    }
}
