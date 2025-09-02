package com.knezevic.edaf.factory.problem;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Problem;

/**
 * A factory for creating {@link Problem} objects.
 */
public interface ProblemFactory {
    /**
     * Creates a {@link Problem} instance.
     *
     * @param config The configuration.
     * @return A {@link Problem} instance.
     * @throws Exception If an error occurs while creating the problem.
     */
    Problem create(Configuration config) throws Exception;
}
