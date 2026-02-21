package com.knezevic.edaf.v3.repr.plugins.discrete;

import com.knezevic.edaf.v3.core.plugins.RepresentationPlugin;
import com.knezevic.edaf.v3.core.util.Params;
import com.knezevic.edaf.v3.repr.impl.CategoricalVectorRepresentation;
import com.knezevic.edaf.v3.repr.types.CategoricalVector;

import java.util.List;
import java.util.Map;

/**
 * Plugin factory for categorical vectors.
 */
public final class CategoricalVectorRepresentationPlugin implements RepresentationPlugin<CategoricalVector> {

    @Override
    public String type() {
        return "categorical-vector";
    }

    @Override
    public String description() {
        return "Categorical vector representation";
    }

    @Override
    public CategoricalVectorRepresentation create(Map<String, Object> params) {
        int length = Params.integer(params, "length", 16);
        List<Object> symbolsRaw = Params.list(params, "symbols");
        List<String> symbols = symbolsRaw.isEmpty()
                ? List.of("A", "B", "C")
                : symbolsRaw.stream().map(String::valueOf).toList();
        return new CategoricalVectorRepresentation(length, symbols);
    }
}
