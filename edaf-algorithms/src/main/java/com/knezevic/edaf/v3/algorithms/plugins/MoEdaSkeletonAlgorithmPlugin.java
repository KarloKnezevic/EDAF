package com.knezevic.edaf.v3.algorithms.plugins;

import com.knezevic.edaf.v3.algorithms.MoEdaSkeletonAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;

import java.util.Map;

/**
 * Plugin for multi-objective EDA skeleton driver.
 */
public final class MoEdaSkeletonAlgorithmPlugin implements AlgorithmPlugin<Object> {

    @Override
    public String type() {
        return "mo-eda-skeleton";
    }

    @Override
    public String description() {
        return "MO-EDA skeleton with TODO for Pareto archive"
                + " and non-dominated sorting";
    }

    @Override
    public Algorithm<Object> create(AlgorithmDependencies<Object> dependencies, Map<String, Object> params) {
        return new MoEdaSkeletonAlgorithm<>(Params.dbl(params, "selectionRatio", 0.5));
    }
}
