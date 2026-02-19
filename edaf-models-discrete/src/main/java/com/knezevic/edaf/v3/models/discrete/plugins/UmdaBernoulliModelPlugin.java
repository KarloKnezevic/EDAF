package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.discrete.BernoulliUmdaModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for UMDA Bernoulli model.
 */
public final class UmdaBernoulliModelPlugin implements ModelPlugin<BitString> {

    @Override
    public String type() {
        return "umda-bernoulli";
    }

    @Override
    public String description() {
        return "Univariate Bernoulli model for UMDA";
    }

    @Override
    public BernoulliUmdaModel create(Map<String, Object> params) {
        return new BernoulliUmdaModel(Params.dbl(params, "smoothing", 0.01));
    }
}
