package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.discrete.HierarchicalBoaModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for hierarchical BOA-style sparse Bayesian-network model.
 */
public final class HierarchicalBoaModelPlugin implements ModelPlugin<BitString> {

    @Override
    public String type() {
        return "hboa-network";
    }

    @Override
    public String description() {
        return "Hierarchical BOA sparse Bayesian-network model";
    }

    @Override
    public HierarchicalBoaModel create(Map<String, Object> params) {
        return new HierarchicalBoaModel(
                Params.dbl(params, "smoothing", 0.5),
                Params.dbl(params, "minMutualInformation", 1e-4),
                Params.dbl(params, "learningRate", 0.8)
        );
    }
}
