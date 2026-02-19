package com.knezevic.edaf.v3.repr.plugins;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.impl.PermutationVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.Map;

/**
 * Plugin factory for permutation vectors.
 */
public final class PermutationVectorRepresentationPlugin implements RepresentationPlugin<PermutationVector> {

    @Override
    public String type() {
        return "permutation-vector";
    }

    @Override
    public String description() {
        return "Permutation representation";
    }

    @Override
    public PermutationVectorRepresentation create(Map<String, Object> params) {
        int size = Params.integer(params, "size", Params.integer(params, "length", 20));
        return new PermutationVectorRepresentation(size);
    }
}
