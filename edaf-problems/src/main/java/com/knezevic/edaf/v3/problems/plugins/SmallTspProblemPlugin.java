package com.knezevic.edaf.v3.problems.plugins;

import com.knezevic.edaf.v3.core.plugins.ProblemPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.problems.SmallTspProblem;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.List;
import java.util.Map;

/**
 * Plugin factory for small TSP problem.
 */
public final class SmallTspProblemPlugin implements ProblemPlugin<PermutationVector> {

    @Override
    public String type() {
        return "small-tsp";
    }

    @Override
    public String description() {
        return "Small Euclidean TSP benchmark";
    }

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
