package com.knezevic.edaf.factory.termination;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.TerminationCondition;

public interface TerminationConditionFactory {
    TerminationCondition create(Configuration config) throws Exception;
}
