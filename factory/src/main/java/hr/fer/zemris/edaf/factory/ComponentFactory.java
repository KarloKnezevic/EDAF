package hr.fer.zemris.edaf.factory;

import hr.fer.zemris.edaf.configuration.pojos.Configuration;
import hr.fer.zemris.edaf.core.Algorithm;
import hr.fer.zemris.edaf.core.Genotype;
import hr.fer.zemris.edaf.core.Population;
import hr.fer.zemris.edaf.core.Problem;
import hr.fer.zemris.edaf.core.Selection;
import hr.fer.zemris.edaf.core.Statistics;
import hr.fer.zemris.edaf.core.TerminationCondition;

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
