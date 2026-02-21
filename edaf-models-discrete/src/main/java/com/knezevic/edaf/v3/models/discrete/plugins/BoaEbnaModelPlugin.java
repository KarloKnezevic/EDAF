package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.discrete.BoaEbnaModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for sparse BOA/EBNA Bayesian-network model.
 */
public final class BoaEbnaModelPlugin implements ModelPlugin<BitString> {

    @Override
    public String type() {
        return "boa-ebna";
    }

    @Override
    public String description() {
        return "BOA/EBNA Bayesian-network model with sparse parent sets";
    }

    @Override
    public BoaEbnaModel create(Map<String, Object> params) {
        return new BoaEbnaModel(
                Params.integer(params, "maxParents", 3),
                Params.dbl(params, "smoothing", 0.5)
        );
    }
}
