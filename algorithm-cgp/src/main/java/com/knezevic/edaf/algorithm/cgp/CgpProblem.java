package com.knezevic.edaf.algorithm.cgp;

import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.genotype.tree.primitives.Function;

import java.util.List;

/**
 * An extension of the {@link Problem} interface for CGP-specific problems.
 * It provides methods to get the number of inputs and outputs for the program graph,
 * as well as the function set to be used.
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
    
    /**
     * Gets the function set to be used in CGP.
     *
     * @return The list of functions available for CGP nodes.
     */
    List<Function> getFunctionSet();
}
