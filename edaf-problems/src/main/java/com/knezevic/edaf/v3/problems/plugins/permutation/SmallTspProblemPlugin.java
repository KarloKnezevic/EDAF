/*
 * Copyright (c) 2026 Dr. Karlo Knezevic
 * Licensed under the Apache License, Version 2.0
 */

package com.knezevic.edaf.v3.problems.plugins.permutation;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.SmallTspProblem;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.List;
import java.util.Map;

/**
 * Plugin factory for small TSP problem.
 * @author Karlo Knezevic
 * @version EDAF 3.0.0
 */
public final class SmallTspProblemPlugin implements ProblemPlugin<PermutationVector> {

    /**
     * Executes type.
     *
     * @return the type
     */
    @Override
    public String type() {
        return "small-tsp";
    }

    /**
     * Executes description.
     *
     * @return the description
     */
    @Override
    public String description() {
        return "Small Euclidean TSP benchmark";
    }

    /**
     * Creates plugin component instance.
     *
     * @param params configuration the input value map
     * @return component instance
     */
    @Override
    public SmallTspProblem create(Map<String, Object> params) {
        List<Object> rawCoords = Params.list(params, "coordinates");
        if (rawCoords.isEmpty()) {
            return new SmallTspProblem(null);
        }

        double[][] coords = new double[rawCoords.size()][2];
        for (int i = 0; i < rawCoords.size(); i++) {
            List<?> pair = (List<?>) rawCoords.get(i);
            coords[i][0] = Double.parseDouble(String.valueOf(pair.get(0)));
            coords[i][1] = Double.parseDouble(String.valueOf(pair.get(1)));
        }
        return new SmallTspProblem(coords);
    }
}
