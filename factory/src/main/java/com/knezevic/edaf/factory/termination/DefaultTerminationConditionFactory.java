package com.knezevic.edaf.factory.termination;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.TerminationCondition;
import com.knezevic.edaf.core.impl.MaxGenerations;

/**
 * A default implementation of the {@link TerminationConditionFactory} interface.
 */
public class DefaultTerminationConditionFactory implements TerminationConditionFactory {
    @Override
    public TerminationCondition create(Configuration config) throws Exception {
        if (config.getAlgorithm().getTermination().getMaxGenerations() > 0) {
            return new MaxGenerations(config.getAlgorithm().getTermination().getMaxGenerations());
        }
        return null;
    }
}
