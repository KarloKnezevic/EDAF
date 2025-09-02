package com.knezevic.edaf.factory.termination;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.TerminationCondition;

/**
 * A factory for creating {@link TerminationCondition} objects.
 */
public interface TerminationConditionFactory {
    /**
     * Creates a {@link TerminationCondition} instance.
     *
     * @param config The configuration.
     * @return A {@link TerminationCondition} instance.
     * @throws Exception If an error occurs while creating the termination condition.
     */
    TerminationCondition create(Configuration config) throws Exception;
}
