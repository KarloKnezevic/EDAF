package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.discrete.BmdaModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for BMDA bivariate dependency model.
 */
public final class BmdaModelPlugin implements ModelPlugin<BitString> {

    @Override
    public String type() {
        return "bmda";
    }

    @Override
    public String description() {
        return "BMDA dependency-tree model with conditional Bernoulli sampling";
    }

    @Override
    public BmdaModel create(Map<String, Object> params) {
        return new BmdaModel(Params.dbl(params, "smoothing", 0.5));
    }
}
