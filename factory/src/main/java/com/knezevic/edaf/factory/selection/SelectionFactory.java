package com.knezevic.edaf.factory.selection;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Selection;

import java.util.Random;

/**
 * A factory for creating {@link Selection} objects.
 */
public interface SelectionFactory {
    /**
     * Creates a {@link Selection} instance.
     *
     * @param config The configuration.
     * @param random The random number generator.
     * @return A {@link Selection} instance.
     * @throws Exception If an error occurs while creating the selection operator.
     */
    Selection create(Configuration config, Random random) throws Exception;
}
