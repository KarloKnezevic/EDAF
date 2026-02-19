package com.knezevic.edaf.v3.repr.plugins;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.impl.VariableLengthVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.VariableLengthVector;

import java.util.Map;

/**
 * Plugin factory for variable-length vectors.
 */
public final class VariableLengthVectorRepresentationPlugin implements RepresentationPlugin<VariableLengthVector<Integer>> {

    @Override
    public String type() {
        return "variable-length-vector";
    }

    @Override
    public String description() {
        return "Variable-length integer token representation";
    }

    @Override
    public VariableLengthVectorRepresentation create(Map<String, Object> params) {
        int min = Params.integer(params, "minLength", 2);
        int max = Params.integer(params, "maxLength", 16);
        int maxToken = Params.integer(params, "maxToken", 64);
        return new VariableLengthVectorRepresentation(min, max, maxToken);
    }
}
