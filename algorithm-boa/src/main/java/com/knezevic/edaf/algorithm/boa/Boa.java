package com.knezevic.edaf.algorithm.boa;

import com.knezevic.edaf.algorithm.boa.acquisition.ExpectedImprovement;
import com.knezevic.edaf.algorithm.boa.surrogate.GaussianProcessSurrogate;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.core.impl.AbstractAlgorithm;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.core.runtime.ExecutionContext;
import com.knezevic.edaf.genotype.fp.FpIndividual;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Random;

public class Boa extends AbstractAlgorithm<FpIndividual> {

    private final int nInit;
    private final int nIter;
    private final TerminationCondition<FpIndividual> terminationCondition;
    private Random random;
    private final GaussianProcessSurrogate surrogate = new GaussianProcessSurrogate();
    private Instances data;
    private final int genotypeLength;
    private final double min;
    private final double max;

    public Boa(Problem<FpIndividual> problem, int nInit, int nIter, int genotypeLength, double min, double max,
               TerminationCondition<FpIndividual> terminationCondition) {
        super(problem, "boa");
        this.nInit = nInit;
        this.nIter = nIter;
        this.genotypeLength = genotypeLength;
        this.min = min;
        this.max = max;
        this.terminationCondition = terminationCondition;
    }

    @Override
    public void setExecutionContext(ExecutionContext context) {
        super.setExecutionContext(context);
        // Use RandomSource from context if available, otherwise fallback to new Random()
        if (context != null && context.getRandomSource() != null) {
            // RandomSource.generator() returns RandomGenerator, extract a seed from it
            // For compatibility with ExpectedImprovement which uses java.util.Random
            java.util.random.RandomGenerator gen = context.getRandomSource().generator();
            // Create a Random with a seed extracted from the generator for reproducibility
            long seed = gen.nextLong();
            this.random = new Random(seed);
        } else if (this.random == null) {
            this.random = new Random();
        }
    }

    @Override
    public void run() {
        publishAlgorithmStarted();
        initialize();

        for (int iteration = 0; iteration < nIter && (terminationCondition == null || !terminationCondition.shouldTerminate(this)); iteration++) {
            setGeneration(iteration);
            try {
                ExpectedImprovement ei = new ExpectedImprovement(surrogate, getBest().getFitness(), random);
                Instance nextPoint = ei.find_max(data);

                FpIndividual newIndividual = new FpIndividual(instanceToDoubleArray(nextPoint));
                long e0 = System.nanoTime();
                problem.evaluate(newIndividual);
                long e1 = System.nanoTime();
                publishEvaluationCompleted(iteration, 1, e1 - e0);

                updateSurrogate(newIndividual);

                updateBestIfBetter(newIndividual);
            } catch (Exception e) {
                // Ignore exceptions during evaluation
            }

            notifyListener(null);
            publishGenerationCompleted(getBest().getFitness(), Double.NaN, Double.NaN, Double.NaN);
        }
        publishAlgorithmTerminated();
    }

    private void initialize() {
        if (random == null) {
            random = new Random();
        }
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 0; i < genotypeLength; i++) {
            attributes.add(new Attribute("x" + i));
        }
        attributes.add(new Attribute("y"));
        data = new Instances("data", attributes, nInit);
        data.setClassIndex(genotypeLength);

        long t0 = System.nanoTime();
        for (int i = 0; i < nInit; i++) {
            double[] genotype = new double[genotypeLength];
            for (int j = 0; j < genotypeLength; j++) {
                genotype[j] = min + (max - min) * random.nextDouble();
            }
            FpIndividual individual = new FpIndividual(genotype);
            problem.evaluate(individual);
            updateSurrogate(individual);
            updateBestIfBetter(individual);
        }
        long t1 = System.nanoTime();
        publishEvaluationCompleted(0, nInit, t1 - t0);
    }

    private void updateSurrogate(FpIndividual individual) {
        double[] values = new double[data.numAttributes()];
        double[] genotype = individual.getGenotype();
        System.arraycopy(genotype, 0, values, 0, genotype.length);
        values[genotype.length] = individual.getFitness();
        data.add(new DenseInstance(1.0, values));
        try {
            surrogate.fit(data);
        } catch (Exception e) {
            // Ignore exceptions during surrogate model fitting
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
    public Population<FpIndividual> getPopulation() {
        SimplePopulation<FpIndividual> pop = new SimplePopulation<>(problem.getOptimizationType());
        if (getBest() != null) pop.add(getBest());
        return pop;
    }
}
