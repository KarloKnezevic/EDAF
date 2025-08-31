package com.knezevic.edaf.statistics.bmda;

import com.knezevic.edaf.core.api.Genotype;
import com.knezevic.edaf.core.api.Population;
import com.knezevic.edaf.core.api.Statistics;
import com.knezevic.edaf.core.impl.SimplePopulation;
import com.knezevic.edaf.genotype.binary.BinaryIndividual;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.net.estimate.SimpleEstimator;
import weka.classifiers.bayes.net.search.local.K2;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

/**
 * BMDA statistics for binary genotypes.
 */
public class BmdaBinaryStatistics implements Statistics<BinaryIndividual> {

    private final Genotype<byte[]> genotype;
    private final Random random;
    private BayesNet bayesNet;
    private Instances instances;

    public BmdaBinaryStatistics(Genotype<byte[]> genotype, Random random) {
        this.genotype = genotype;
        this.random = random;
        this.bayesNet = new BayesNet();
        K2 searchAlgorithm = new K2();
        searchAlgorithm.setMaxNrOfParents(1);
        this.bayesNet.setSearchAlgorithm(searchAlgorithm);
        this.bayesNet.setEstimator(new SimpleEstimator());
    }

    @Override
    public void estimate(Population<BinaryIndividual> population) {
        // Create the header for the dataset
        ArrayList<Attribute> attributes = new ArrayList<>();
        for (int i = 0; i < genotype.getLength(); i++) {
            ArrayList<String> values = new ArrayList<>();
            values.add("0");
            values.add("1");
            attributes.add(new Attribute("A" + i, values));
        }
        this.instances = new Instances("data", attributes, population.getSize());
        this.instances.setClassIndex(0); // Not used, but required by Weka

        // Add the data
        for (BinaryIndividual individual : population) {
            Instance inst = new DenseInstance(genotype.getLength());
            for (int i = 0; i < genotype.getLength(); i++) {
                inst.setValue(i, individual.getGenotype()[i]);
            }
            this.instances.add(inst);
        }

        try {
            this.bayesNet.buildClassifier(this.instances);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(BinaryIndividual individual, double learningRate) {
        // Not used by BMDA
    }

    @Override
    public Population<BinaryIndividual> sample(int size) {
        Population<BinaryIndividual> newPopulation = new SimplePopulation<>();
        try {
            for (int i = 0; i < size; i++) {
                byte[] newGenotype = new byte[genotype.getLength()];
                Instance sampleInstance = new DenseInstance(genotype.getLength());
                sampleInstance.setDataset(this.instances);

                // Sample from the bayesian network
                for (int j = 0; j < genotype.getLength(); j++) {
                    double[] dist = bayesNet.distributionForInstance(sampleInstance);
                    if (random.nextDouble() < dist[1]) {
                        sampleInstance.setValue(j, 1);
                        newGenotype[j] = 1;
                    } else {
                        sampleInstance.setValue(j, 0);
                        newGenotype[j] = 0;
                    }
                }
                newPopulation.add(new BinaryIndividual(newGenotype));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newPopulation;
    }
}
