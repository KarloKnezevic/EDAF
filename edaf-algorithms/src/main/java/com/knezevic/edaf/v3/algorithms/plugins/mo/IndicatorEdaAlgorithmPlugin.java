package com.knezevic.edaf.v3.algorithms.plugins.mo;

import com.knezevic.edaf.v3.algorithms.mo.IndicatorEdaAlgorithm;
import com.knezevic.edaf.v3.core.api.Algorithm;
import com.knezevic.edaf.v3.core.plugins.AlgorithmDependencies;
import com.knezevic.edaf.v3.core.plugins.AlgorithmPlugin;
import com.knezevic.edaf.v3.core.util.Params;

import java.util.Map;

/**
 * Plugin for Indicator-based EDA driver
 */
public final class IndicatorEdaAlgorithmPlugin implements AlgorithmPlugin<Object> {

    @Override
    public String type() {
        return "indicator-eda";
    }

    @Override
    public String description() {
        return "Indicator-based EDA driver";
    }

    @Override
    public Algorithm<Object> create(AlgorithmDependencies<Object> dependencies, Map<String, Object> params) {
        return new IndicatorEdaAlgorithm(Params.dbl(params, "selectionRatio", 0.5));
    }
}
