package com.knezevic.edaf.v3.core.plugins;

import com.knezevic.edaf.v3.core.api.Representation;

import java.util.Map;

/**
 * Plugin factory for representation components.
 */
public interface RepresentationPlugin<G> extends Plugin {

    /**
     * Constructs a representation instance from config parameters.
     */
    Representation<G> create(Map<String, Object> params);
}
