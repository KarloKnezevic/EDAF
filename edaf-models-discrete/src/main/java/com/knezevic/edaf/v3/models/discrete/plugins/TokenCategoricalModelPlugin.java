package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.models.discrete.token.TokenCategoricalModel;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.util.Map;

/**
 * Plugin factory for variable-length token categorical model.
 */
public final class TokenCategoricalModelPlugin implements ModelPlugin<VariableLengthVector<Integer>> {

    @Override
    public String type() {
        return "token-categorical";
    }

    @Override
    public String description() {
        return "Categorical model for variable-length integer token sequences";
    }

    @Override
    public TokenCategoricalModel create(Map<String, Object> params) {
        int maxToken = Params.integer(params, "maxToken", 64);
        double smoothing = Params.dbl(params, "smoothing", 0.01);
        return new TokenCategoricalModel(maxToken, smoothing);
    }
}
