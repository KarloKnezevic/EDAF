package com.knezevic.edaf.factory.problem;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Problem;

public interface ProblemFactory {
    Problem create(Configuration config) throws Exception;
}
