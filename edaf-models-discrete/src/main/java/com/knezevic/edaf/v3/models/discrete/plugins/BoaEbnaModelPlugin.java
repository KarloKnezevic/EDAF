package com.knezevic.edaf.v3.models.discrete.plugins;

import com.knezevic.edaf.v3.core.plugins.ModelPlugin;
import com.knezevic.edaf.v3.models.discrete.BoaEbnaModel;
import com.knezevic.edaf.v3.repr.types.BitString;

import java.util.Map;

/**
 * Plugin factory for BOA/EBNA scaffold model.
 */
public final class BoaEbnaModelPlugin implements ModelPlugin<BitString> {

    @Override
    public String type() {
        return "boa-ebna";
    }

    @Override
    public String description() {
        return "BOA/EBNA scaffold model with Bayesian-network TODO";
    }

    @Override
    public BoaEbnaModel create(Map<String, Object> params) {
        return new BoaEbnaModel();
    }
}
