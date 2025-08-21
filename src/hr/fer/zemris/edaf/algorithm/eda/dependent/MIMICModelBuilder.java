package hr.fer.zemris.edaf.algorithm.eda.dependent;

import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.binary.Binary;
import hr.fer.zemris.edaf.statistics.reprezentation.IStatReprezentation;
import hr.fer.zemris.edaf.statistics.reprezentation.binary.Chain;

import java.util.Arrays;
import java.util.Random;

/**
 * Builds a chain model for the Mutual Information Maximizing Input Clustering (MIMIC) algorithm.
 * <p>
 * The class takes a set of selected individuals and builds a probabilistic model based on
 * pairwise mutual information between the variables. The model is a chain, where the variables
 * are ordered to maximize the mutual information between adjacent variables in the chain.
 *
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * @author Jules
 */
public class MIMICModelBuilder {

    private final Random rand;
    private final double[] marginalProbabilities;
    private final double[][] jointProbabilities;
    private final int genotypeLength;
    private final byte[][] samples;
    private final int numSamples;

    /**
     * Constructs a MIMICModelBuilder.
     *
     * @param marginalProbabilities the marginal probabilities of each variable being 1.
     * @param selectedIndividuals   the individuals selected from the population.
     */
    public MIMICModelBuilder(double[] marginalProbabilities, Individual[] selectedIndividuals) {
        this.marginalProbabilities = marginalProbabilities;
        this.numSamples = selectedIndividuals.length;
        this.genotypeLength = selectedIndividuals[0].getGenotypeLength();
        this.rand = selectedIndividuals[0].getRand();

        this.samples = new byte[numSamples][genotypeLength];
        for (int i = 0; i < numSamples; i++) {
            this.samples[i] = ((Binary) selectedIndividuals[i]).getBits();
        }

        this.jointProbabilities = new double[genotypeLength][genotypeLength];
        computeJointProbabilities();
    }

    /**
     * Builds the chain model for the MIMIC algorithm.
     *
     * @return the chain model.
     */
    public IStatReprezentation buildChain() {
        final Chain chain = new Chain(genotypeLength);
        chain.setPSingle(marginalProbabilities);

        final boolean[] used = new boolean[genotypeLength];
        Arrays.fill(used, false);

        int previousVariable = getStartVariable();
        used[previousVariable] = true;
        chain.set(previousVariable, 0);

        for (int i = 1; i < genotypeLength; i++) {
            int nextVariable = getNextVariable(used, previousVariable);
            used[nextVariable] = true;
            chain.set(nextVariable, jointProbabilities[previousVariable][nextVariable]);
            previousVariable = nextVariable;
        }

        return chain;
    }

    /**
     * Computes the joint probabilities for all pairs of variables.
     */
    private void computeJointProbabilities() {
        for (int i = 0; i < genotypeLength; i++) {
            for (int j = i; j < genotypeLength; j++) {
                if (i == j) {
                    jointProbabilities[i][j] = marginalProbabilities[i];
                } else {
                    double p11 = 0;
                    for (int k = 0; k < numSamples; k++) {
                        if (samples[k][i] == 1 && samples[k][j] == 1) {
                            p11++;
                        }
                    }
                    p11 /= numSamples;
                    jointProbabilities[i][j] = p11;
                    jointProbabilities[j][i] = p11;
                }
            }
        }
    }

    /**
     * Gets the starting variable for the chain, which is the one with the highest entropy.
     *
     * @return the index of the starting variable.
     */
    private int getStartVariable() {
        int startVariable = -1;
        double maxEntropy = -1;

        for (int i = 0; i < genotypeLength; i++) {
            double entropy = calculateEntropy(marginalProbabilities[i]);
            if (entropy > maxEntropy) {
                maxEntropy = entropy;
                startVariable = i;
            }
        }
        return startVariable;
    }

    /**
     * Gets the next variable to add to the chain. The next variable is the one that minimizes
     * the conditional entropy with the previous variable in the chain.
     *
     * @param used             an array indicating which variables have already been added to the chain.
     * @param previousVariable the previous variable in the chain.
     * @return the index of the next variable to add to the chain.
     */
    private int getNextVariable(boolean[] used, int previousVariable) {
        int nextVariable = -1;
        double minConditionalEntropy = Double.MAX_VALUE;

        for (int i = 0; i < genotypeLength; i++) {
            if (!used[i]) {
                double conditionalEntropy = calculateConditionalEntropy(i, previousVariable);
                if (conditionalEntropy < minConditionalEntropy) {
                    minConditionalEntropy = conditionalEntropy;
                    nextVariable = i;
                }
            }
        }
        return nextVariable;
    }

    /**
     * Calculates the entropy of a single binary variable.
     * H(X) = -p(x=1)*log2(p(x=1)) - p(x=0)*log2(p(x=0))
     *
     * @param p1 the probability of the variable being 1.
     * @return the entropy of the variable.
     */
    private double calculateEntropy(double p1) {
        if (p1 == 0 || p1 == 1) {
            return 0;
        }
        double p0 = 1 - p1;
        return -p1 * Math.log(p1) / Math.log(2) - p0 * Math.log(p0) / Math.log(2);
    }

    /**
     * Calculates the conditional entropy H(X|Y) between two binary variables.
     * H(X|Y) = - sum_{x,y} p(x,y) * log2(p(x|y))
     *
     * @param x the index of the first variable.
     * @param y the index of the second variable.
     * @return the conditional entropy H(X|Y).
     */
    private double calculateConditionalEntropy(int x, int y) {
        double p_y1 = marginalProbabilities[y];
        double p_y0 = 1 - p_y1;

        double p_x1y1 = jointProbabilities[x][y];
        double p_x0y1 = p_y1 - p_x1y1;
        
        double p_x1 = marginalProbabilities[x];
        double p_x1y0 = p_x1 - p_x1y1;
        double p_x0y0 = p_y0 - p_x1y0;


        double h = 0;

        if (p_y1 > 0) {
            if (p_x1y1 > 0) h -= p_x1y1 * Math.log(p_x1y1 / p_y1) / Math.log(2);
            if (p_x0y1 > 0) h -= p_x0y1 * Math.log(p_x0y1 / p_y1) / Math.log(2);
        }
        
        if (p_y0 > 0) {
            if (p_x1y0 > 0) h -= p_x1y0 * Math.log(p_x1y0 / p_y0) / Math.log(2);
            if (p_x0y0 > 0) h -= p_x0y0 * Math.log(p_x0y0 / p_y0) / Math.log(2);
        }

        return h;
    }
}
