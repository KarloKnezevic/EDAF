package com.knezevic.edaf.v3.repr.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.impl.BitStringRepresentation;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for bitstring representation.
 */
public final class BitStringRepresentationPlugin implements RepresentationPlugin<BitString> {

    @Override
    public String type() {
        return "bitstring";
    }

    @Override
    public String description() {
        return "Binary vector representation";
    }

    @Override
    public BitStringRepresentation create(Map<String, Object> params) {
        int length = Params.integer(params, "length", 64);
        return new BitStringRepresentation(length);
    }
}
