package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.core.api.Problem;

/**
 * An extension of the {@link Problem} interface for CGP-specific problems.
 * It provides methods to get the number of inputs and outputs for the program graph.
 */
public interface CgpProblem extends Problem<CgpIndividual> {

    /**
     * Gets the number of inputs for the program.
     *
     * @return The number of inputs.
     */
    int getNumInputs();

    /**
     * Gets the number of outputs for the program.
     *
     * @return The number of outputs.
     */
    int getNumOutputs();
}
