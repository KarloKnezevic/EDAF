package com.knezevic.edaf.v3.repr.plugins.continuous;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.impl.RealVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.RealVector;

import java.util.Map;

/**
 * Plugin factory for real-valued vectors.
 */
public final class RealVectorRepresentationPlugin implements RepresentationPlugin<RealVector> {

    @Override
    public String type() {
        return "real-vector";
    }

    @Override
    public String description() {
        return "Real-valued vector representation";
    }

    @Override
    public RealVectorRepresentation create(Map<String, Object> params) {
        int length = Params.integer(params, "length", 16);
        double lower = Params.dbl(params, "lower", -5.0);
        double upper = Params.dbl(params, "upper", 5.0);
        return new RealVectorRepresentation(length, lower, upper);
    }
}
