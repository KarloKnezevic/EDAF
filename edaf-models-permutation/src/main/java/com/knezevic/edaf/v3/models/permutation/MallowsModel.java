package com.knezevic.edaf.v3.models.permutation;

import com.knezevic.edaf.v3.core.api.ConstraintHandling;
import com.knezevic.edaf.v3.core.api.Individual;
import com.knezevic.edaf.v3.core.api.Model;
import com.knezevic.edaf.v3.core.api.ModelDiagnostics;
import com.knezevic.edaf.v3.core.api.Problem;
import com.knezevic.edaf.v3.core.api.Representation;
import com.knezevic.edaf.v3.core.rng.RngStream;
import com.knezevic.edaf.v3.repr.types.PermutationVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mallows-Kendall model with consensus ranking and repeated-insertion sampling.
 */
public final class MallowsModel implements Model<PermutationVector> {

    private final double minPhi;
    private final double maxPhi;

    private int[] consensus;
    private double phi;
    private double theta;

    public MallowsModel(double minPhi, double maxPhi) {
        this.minPhi = Math.max(1.0e-4, Math.min(0.99, minPhi));
        this.maxPhi = Math.max(this.minPhi, Math.min(0.9999, maxPhi));
    }

    @Override
    public String name() {
        return "mallows";
    }

    @Override
    public void fit(List<Individual<PermutationVector>> selected,
                    Representation<PermutationVector> representation,
                    RngStream rng) {
        if (selected == null || selected.isEmpty()) {
            return;
        }

        int size = selected.getFirst().genotype().size();
        double[] score = new double[size];
        for (Individual<PermutationVector> individual : selected) {
            int[] order = individual.genotype().order();
            for (int position = 0; position < order.length; position++) {
                score[order[position]] += (size - position);
            }
        }

        Integer[] items = new Integer[size];
        for (int i = 0; i < size; i++) {
            items[i] = i;
        }
        Arrays.sort(items, (a, b) -> Double.compare(score[b], score[a]));
        consensus = new int[size];
        for (int i = 0; i < size; i++) {
            consensus[i] = items[i];
        }

        double averageDistance = 0.0;
        for (Individual<PermutationVector> individual : selected) {
            averageDistance += kendallDistance(consensus, individual.genotype().order());
        }
        averageDistance /= selected.size();
        double maxDistance = size * (size - 1.0) / 2.0;
        double normalized = maxDistance <= 0.0 ? 0.0 : averageDistance / maxDistance;

        this.phi = clamp(0.05 + 0.90 * normalized, minPhi, maxPhi);
        this.theta = -Math.log(phi);
    }

    @Override
    public List<PermutationVector> sample(int count,
                                          Representation<PermutationVector> representation,
                                          Problem<PermutationVector> problem,
                                          ConstraintHandling<PermutationVector> constraintHandling,
                                          RngStream rng) {
        if (consensus == null) {
            throw new IllegalStateException("Model must be fitted before sampling");
        }

        List<PermutationVector> samples = new ArrayList<>(count);
        for (int sampleIndex = 0; sampleIndex < count; sampleIndex++) {
            int[] order = sampleOnceRim(rng);
            PermutationVector candidate = new PermutationVector(order);
            samples.add(constraintHandling.enforce(candidate, representation, problem, rng));
        }
        return samples;
    }

    @Override
    public ModelDiagnostics diagnostics() {
        if (consensus == null) {
            return ModelDiagnostics.empty();
        }
        Map<String, Double> values = new LinkedHashMap<>();
        values.put("mallows_theta", theta);
        values.put("mallows_phi", phi);
        values.put("mallows_size", (double) consensus.length);
        return new ModelDiagnostics(values);
    }

    private int[] sampleOnceRim(RngStream rng) {
        List<Integer> ranking = new ArrayList<>(consensus.length);
        for (int i = 0; i < consensus.length; i++) {
            int item = consensus[i];
            int position = sampleInsertionPosition(i, rng);
            ranking.add(position, item);
        }
        int[] result = new int[ranking.size()];
        for (int i = 0; i < ranking.size(); i++) {
            result[i] = ranking.get(i);
        }
        return result;
    }

    private int sampleInsertionPosition(int iteration, RngStream rng) {
        int states = iteration + 1;
        double[] probability = new double[states];
        double sum = 0.0;
        for (int position = 0; position < states; position++) {
            int inversionsAdded = iteration - position;
            probability[position] = Math.pow(phi, inversionsAdded);
            sum += probability[position];
        }

        double target = rng.nextDouble() * sum;
        double cumulative = 0.0;
        for (int position = 0; position < states; position++) {
            cumulative += probability[position];
            if (cumulative >= target) {
                return position;
            }
        }
        return states - 1;
    }

    private static int kendallDistance(int[] left, int[] right) {
        int n = left.length;
        int[] position = new int[n];
        for (int i = 0; i < n; i++) {
            position[left[i]] = i;
        }
        int[] mapped = new int[n];
        for (int i = 0; i < n; i++) {
            mapped[i] = position[right[i]];
        }
        int distance = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (mapped[i] > mapped[j]) {
                    distance++;
                }
            }
        }
        return distance;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
