package com.knezevic.edaf.v3.repr.plugins;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.impl.IntVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.IntVector;

import java.util.Map;

/**
 * Plugin factory for bounded integer vectors.
 */
public final class IntVectorRepresentationPlugin implements RepresentationPlugin<IntVector> {

    @Override
    public String type() {
        return "int-vector";
    }

    @Override
    public String description() {
        return "Bounded integer vector representation";
    }

    @Override
    public IntVectorRepresentation create(Map<String, Object> params) {
        int length = Params.integer(params, "length", 32);
        int min = Params.integer(params, "min", 0);
        int max = Params.integer(params, "max", 10);
        return new IntVectorRepresentation(length, min, max);
    }
}
