package com.knezevic.edaf.factory.genotype;

import com.knezevic.edaf.configuration.pojos.Configuration;
import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.genotype.tree.Node;
import com.knezevic.edaf.genotype.tree.TreeGenotype;
import com.knezevic.edaf.genotype.tree.TreeIndividual;
import com.knezevic.edaf.genotype.tree.primitives.PrimitiveSet;

import java.util.Random;

/**
 * A factory for creating {@link TreeGenotype} objects.
 */
public class TreeGenotypeFactory implements GenotypeFactory {

    @Override
    public Genotype create(Configuration config, Random random) {
        int maxDepth = config.getProblem().getGenotype().getMaxDepth();

        // Use the new centralized factory to create the primitive set
        PrimitiveSet primitiveSet = PrimitiveSetFactory.create(config);

        return new TreeGenotype(primitiveSet, maxDepth, random);
    }

    /**
     * Creates a {@link TreeIndividual} from the given genotype.
     *
     * @param genotype The genotype.
     * @return A {@link TreeIndividual} instance.
     */
    public Individual createIndividual(Object genotype) {
        if (!(genotype instanceof Node)) {
            throw new IllegalArgumentException("Genotype for TreeIndividual must be a Node.");
        }
        return new TreeIndividual((Node) genotype);
    }
}
