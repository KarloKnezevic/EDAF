package hr.fer.zemris.edaf.statistics.reprezentation.binary;

import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.binary.Binary;
import hr.fer.zemris.edaf.statistics.distributions.ChiSquare;
import hr.fer.zemris.edaf.statistics.reprezentation.IStatReprezentation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Methods and operations for binary reprezentation.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class BinaryReprezentation {

	private final Random rand;

	private final double[] pSingle;

	private double[][] pJointOnes;

	private final int sampleLength;

	private final byte[][] samples;

	public BinaryReprezentation(double[] pSIngle, Individual[] smpls) {
		pSingle = pSIngle;
		Binary b = (Binary) smpls[0];
		sampleLength = b.getGenotypeLength();
		rand = b.getRand();

		samples = new byte[smpls.length][sampleLength];
		for (int i = 0; i < smpls.length; i++) {
			b = (Binary) smpls[i];
			samples[i] = b.getBits();
		}
	}

	/**
	 * CHAIN REPRESENTATION
	 * 
	 * @return CHAIN REPRESENTATION
	 */
	public IStatReprezentation getChain() {
		final Chain chain = new Chain(sampleLength);

		final boolean[] used = new boolean[sampleLength];
		Arrays.fill(used, false);

		chain.setPSingle(pSingle);

		computeJointOnes();

		int prev = getIndexMinEntropy(pSingle);
		chain.set(prev, 0);
		used[prev] = true;

		for (int i = 1; i < sampleLength; i++) {
			final int chosenIndex = getIndexMinJointEntropy(used, prev,
					pJointOnes, pSingle);
			chain.set(chosenIndex, pJointOnes[prev][chosenIndex]);
			prev = chosenIndex;
			used[prev] = true;
		}

		return chain;
	}

	/**
	 * Method returns tree structure where maximum parent count is one. Using
	 * ChiSquare statistics for dependency computing.
	 * 
	 * @return tree where maximum parent count is one
	 */
	public IStatReprezentation getTree() {
		// create tree
		final Tree tree = new Tree(sampleLength);
		// create statistics
		final ChiSquare chiStat = new ChiSquare(sampleLength);

		// compute single and joint distribution
		tree.setPSingle(pSingle);
		computeJointOnes();

		// index list of all elements
		final int[] indexList = new int[sampleLength];
		// list for nodes having parent
		final Set<Integer> hasParent = new HashSet<>();

		// create indexes and shuffle
		for (int i = 0; i < sampleLength; i++) {
			indexList[i] = i;
		}
		shuffleArray(indexList);

		final int len = indexList.length;
		for (int i = 0; i < len; i++) {
			final int parentIndex = indexList[i];
			for (int j = 0; j < len; j++) {
				final int childIndex = indexList[j];
				// node can not be itself parent
				if ((i == j) || hasParent.contains(childIndex)
						|| (tree.getParent(parentIndex) == childIndex)) {
					continue;
				}

				// if nodes are dependent and node has not parent
				if (chiStat.isDependent(pSingle[parentIndex],
						pSingle[childIndex],
						pJointOnes[parentIndex][childIndex])) {

					hasParent.add(childIndex);
					// insert node in tree
					tree.set(childIndex, parentIndex,
							pJointOnes[parentIndex][childIndex]);
				}
			}
		}

		// for each node that has not parent, set independent
		for (final int i : indexList) {
			if (!hasParent.contains(i)) {
				tree.setIndependent(i);
			}
		}

		return tree;
	}

	/**
	 * JOINT PROBABILITY
	 */
	private void computeJointOnes() {
		pJointOnes = new double[sampleLength][sampleLength];

		pJointOnes[pSingle.length - 1][pSingle.length - 1] = pSingle[pSingle.length - 1];
		for (int column1 = 0; column1 < (sampleLength - 1); column1++) {
			pJointOnes[column1][column1] = pSingle[column1];
			for (int column2 = column1 + 1; column2 < sampleLength; column2++) {
				for (int row = 0; row < samples.length; row++) {
					if ((samples[row][column1] & samples[row][column2]) == 1) {
						pJointOnes[column1][column2] += 1.0 / samples.length;
						pJointOnes[column2][column1] = pJointOnes[column1][column2];
					}
				}
			}
		}
	}

	private int getIndexMinEntropy(double[] probab) {
		int index = 0;
		double min = 0;
		for (int i = 0; i < probab.length; i++) {
			final double res = entropy(probab[i]);
			if ((i == 0) || (res < min) || ((res == min) && rand.nextBoolean())) {
				min = res;
				index = i;
			}
		}
		return index;
	}

	private int getIndexMinJointEntropy(boolean[] used, int prev,
			double[][] joint, double[] probab) {
		int index = -1;
		double min = 0;

		for (int i = 0; i < used.length; i++) {
			if (used[i]) {
				continue;
			}

			final double res = entropy(joint[prev][i], joint[prev][i]
					/ probab[prev]);
			if ((index == -1) || (res < min)
					|| ((res == min) && rand.nextBoolean())) {
				min = res;
				index = i;
			}
		}
		return index;
	}

	private double entropy(double value) {
		if (value == 0.0) {
			return 0;
		}
		return -((value * Math.log(value)) + ((1 - value) * Math.log(1 - value)));
	}

	private double entropy(double jointValue, double condValue) {
		if (condValue == 0.0) {
			return 0;
		}
		return -(jointValue * Math.log(condValue));
	}

	/**
	 * ARRAY SHUFFLE
	 * 
	 * @param array
	 */
	private void shuffleArray(int[] array) {
		for (int i = array.length; i > 1; i--) {
			final int b = rand.nextInt(i);
			if (b != (i - 1)) {
				final int e = array[i - 1];
				array[i - 1] = array[b];
				array[b] = e;
			}
		}

	}
}