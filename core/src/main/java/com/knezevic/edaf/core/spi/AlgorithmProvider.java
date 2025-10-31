package com.knezevic.edaf.core.spi;
import com.knezevic.edaf.core.api.Algorithm;
import com.knezevic.edaf.core.api.Crossover;
import com.knezevic.edaf.core.api.Mutation;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.core.api.Selection;
import com.knezevic.edaf.core.api.Statistics;
import com.knezevic.edaf.core.api.TerminationCondition;

import java.util.Random;

/**
 * SPI for discovering algorithm implementations at runtime.
 * Implementations should be registered via META-INF/services.
 */
public interface AlgorithmProvider {

    /**
     * Unique identifier used in configuration (e.g. "umda", "gga", "ega").
     */
    String id();

    /**
     * Returns true if this algorithm supports the given genotype and problem types.
     */
    boolean supports(Class<?> genotypeType, Class<?> problemType);

    /**
     * Construct a new algorithm instance using provided components and configuration.
     */
    Algorithm<?> create(
                     Problem<?> problem,
                     Population<?> population,
                     Selection<?> selection,
                     Crossover<?> crossover,
                     Mutation<?> mutation,
                     Statistics<?> statistics,
                     TerminationCondition<?> terminationCondition,
                     Random random,
                     int selectionSize,
                     int genotypeLength);
}


