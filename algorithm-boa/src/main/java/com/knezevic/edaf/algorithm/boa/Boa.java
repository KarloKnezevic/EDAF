package com.knezevic.edaf.algorithm.boa;

import com.knezevic.edaf.algorithm.boa.acquisition.ExpectedImprovement;
import com.knezevic.edaf.algorithm.boa.surrogate.GaussianProcessSurrogate;
import com.knezevic.edaf.core.api.*;
import com.knezevic.edaf.genotype.fp.FpIndividual;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Random;

public class Boa implements Algorithm<FpIndividual> {

    private final Problem<FpIndividual> problem;
    private final int nInit;
    private final int nIter;
    private FpIndividual best;
    private int iteration;
    private ProgressListener listener;
    private final Random random = new Random();
    private final GaussianProcessSurrogate surrogate = new GaussianProcessSurrogate();
    private Instances data;
    private final int genotypeLength;
    private final double min;
    private final double max;

    public Boa(Problem<FpIndividual> problem, int nInit, int nIter, int genotypeLength, double min, double max) {
        this.problem = problem;
        this.nInit = nInit;
        this.nIter = nIter;
        this.genotypeLength = genotypeLength;
        this.min = min;
        this.max = max;
    }

    @Override
    public void run() {
        initialize();

        for (iteration = 0; iteration < nIter; iteration++) {
            try {
                ExpectedImprovement ei = new ExpectedImprovement(surrogate, best.getFitness(), random);
                Instance nextPoint = ei.find_max(data);

                FpIndividual newIndividual = new FpIndividual(instanceToDoubleArray(nextPoint));
                problem.evaluate(newIndividual);

                updateSurrogate(newIndividual);

                if (best == null || newIndividual.getFitness() < best.getFitness()) {
                    best = newIndividual.copy();
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
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 0; i < genotypeLength; i++) {
            attributes.add(new Attribute("x" + i));
        }
        attributes.add(new Attribute("y"));
        data = new Instances("data", attributes, nInit);
        data.setClassIndex(genotypeLength);

        for (int i = 0; i < nInit; i++) {
            double[] genotype = new double[genotypeLength];
            for (int j = 0; j < genotypeLength; j++) {
                genotype[j] = min + (max - min) * random.nextDouble();
            }
            FpIndividual individual = new FpIndividual(genotype);
            problem.evaluate(individual);
            updateSurrogate(individual);
            if (best == null || individual.getFitness() < best.getFitness()) {
                best = individual.copy();
            }
        }
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
    public FpIndividual getBest() {
        return best;
    }

    @Override
    public int getGeneration() {
        return iteration;
    }

    @Override
    public Population<FpIndividual> getPopulation() {
        return null;
    }

    @Override
    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }
}
