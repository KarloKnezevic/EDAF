package com.knezevic.edaf.algorithm.boa;

import com.knezevic.edaf.algorithm.boa.acquisition.ExpectedImprovement;
import com.knezevic.edaf.algorithm.boa.surrogate.GaussianProcessSurrogate;
import com.knezevic.edaf.core.api.Algorithm;
import com.knezevic.edaf.core.api.Individual;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Problem;
import com.knezevic.edaf.core.api.ProgressListener;
import com.knezevic.edaf.genotype.fp.FpIndividual;
import com.knezevic.edaf.testing.ContinuousProblem;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Random;

/**
 * Bayesian Optimization Algorithm (BOA).
 *
 * @param <T> The type of individual in the population.
 */
public class Boa<T extends Individual> implements Algorithm<T> {

    private final Problem<T> problem;
    private final int nInit;
    private final int nIter;
    private T best;
    private int iteration;
    private ProgressListener listener;
    private final Random random = new Random();
    private final GaussianProcessSurrogate surrogate = new GaussianProcessSurrogate();
    private Instances data;

    public Boa(Problem<T> problem, int nInit, int nIter) {
        this.problem = problem;
        this.nInit = nInit;
        this.nIter = nIter;
    }

    @Override
    public void run() {
        // 1. Initialize
        initialize();

        // 2. Run iterations
        for (iteration = 0; iteration < nIter; iteration++) {
            try {
                // 2.1. Find next point to sample
                ExpectedImprovement ei = new ExpectedImprovement(surrogate, best.getFitness(), random);
                Instance nextPoint = ei.find_max(data);

                // 2.2. Evaluate objective function
                // This is a hack, as we don't have a way to create an individual from a double array.
                // This will only work for FpIndividual.
                T newIndividual = (T) new FpIndividual(instanceToDoubleArray(nextPoint));
                problem.evaluate(newIndividual);

                // 2.3. Update surrogate model
                updateSurrogate(newIndividual);

                // 2.4. Update best
                if (best == null || newIndividual.getFitness() < best.getFitness()) {
                    best = (T) newIndividual.copy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (listener != null) {
                listener.onGenerationDone(iteration, best, null);
            }
        }
    }

    private void initialize() {
        // This is a hack, as we don't have a way to get the genotype length.
        int numAttributes = 2; // harcoded for BBOB problem

        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 0; i < numAttributes; i++) {
            attributes.add(new Attribute("x" + i));
        }
        attributes.add(new Attribute("y"));
        data = new Instances("data", attributes, nInit);
        data.setClassIndex(numAttributes);

        for (int i = 0; i < nInit; i++) {
            double[] genotype = new double[numAttributes];
            for(int j = 0; j < numAttributes; j++) {
                genotype[j] = random.nextDouble() * 10 - 5; // BBOB range
            }
            T individual = (T) ((ContinuousProblem)problem).createIndividual(genotype);
            problem.evaluate(individual);
            updateSurrogate(individual);
            if (best == null || individual.getFitness() < best.getFitness()) {
                best = (T) individual.copy();
            }
        }
    }

    private void updateSurrogate(T individual) {
        double[] values = new double[data.numAttributes()];
        double[] genotype = ((FpIndividual) individual).getGenotype();
        System.arraycopy(genotype, 0, values, 0, genotype.length);
        values[genotype.length] = individual.getFitness();
        data.add(new DenseInstance(1.0, values));
        try {
            surrogate.fit(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double[] instanceToDoubleArray(Instance instance) {
        double[] arr = new double[instance.numAttributes() - 1];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = instance.value(i);
        }
        return arr;
    }


    @Override
    public T getBest() {
        return best;
    }

    @Override
    public int getGeneration() {
        return iteration;
    }

    @Override
    public Population<T> getPopulation() {
        // BOA is not a population-based algorithm in the same way as other EDAs.
        // We can return null or a population with the evaluated points.
        return null;
    }

    @Override
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }
}
