package com.knezevic.edaf.factory;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.*;

import java.util.Random;

/**
 * A factory for creating components of the framework.
 */
public interface ComponentFactory {

    Problem createProblem(Configuration config) throws Exception;

    Genotype createGenotype(Configuration config, Random random) throws Exception;

    Population createPopulation(Configuration config, Genotype genotype) throws Exception;

    Statistics createStatistics(Configuration config, Genotype genotype, Random random) throws Exception;

    Selection createSelection(Configuration config, Random random) throws Exception;

    TerminationCondition createTerminationCondition(Configuration config) throws Exception;

    Algorithm createAlgorithm(Configuration config, Problem problem, Population population,
                              Selection selection, Statistics statistics,
                              TerminationCondition terminationCondition, Random random) throws Exception;
}
