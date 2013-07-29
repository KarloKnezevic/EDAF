package hr.fer.zemris.edaf.statistics.reprezentation.binary;

import hr.fer.zemris.edaf.statistics.reprezentation.IStatReprezentation;

import java.util.Arrays;

/**
 * CHAIN REPRESENATION
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class Chain implements IStatReprezentation {

	private final double epsilon = 0.05;

	/**
	 * pi vector of indexes chain example: 3->5->8->1->0->...
	 */
	private final int[] indexes;

	/**
	 * Joint probability. i-th place is joint probability of (i-th | front of
	 * i-th in indexes).
	 */
	private final double[] pJoint;

	/**
	 * Single probabilities of elements.
	 */
	private double[] pSingle;

	private final int[] values;

	private int counter;

	private int pointerIndex;

	public Chain(int length) {
		indexes = new int[length];
		pJoint = new double[length];
		pSingle = new double[length];
		values = new int[length];
		counter = 0;
		pointerIndex = -1;
	}

	public void setPSingle(double[] pSingle) {
		this.pSingle = pSingle;
	}

	public void set(int index, double joint) {
		indexes[counter] = index;

		// added for P!=0
		if (joint == 0.0) {
			joint = epsilon;
		}

		pJoint[index] = joint;
		counter++;
	}

	@Override
	public double getLength() {
		return indexes.length;
	}

	@Override
	public boolean hasMoreProbabilities() {
		if (pointerIndex < (getLength() - 1)) {
			return true;
		}

		reset();
		return false;
	}

	@Override
	public int[] getIndexesInRepresentation() {
		pointerIndex++;
		return new int[] { indexes[pointerIndex] };
	}

	@Override
	public double getProbability(int index) {
		// if head, return single probability for head
		if (pointerIndex == 0) {
			return pSingle[index];
		}

		/*
		 * 1. check generated value of previous element 2. if it is one, return
		 * p(x|y) = pjoint(x,y) / p(y) 3. if it is zerro, return p(x|y) =
		 * pjoint(x,y) / (1-p(y))
		 */

		return values[indexes[pointerIndex - 1]] == 1 ? pJoint[index]
				/ pSingle[indexes[pointerIndex - 1]] : pJoint[index]
				/ (1.0 - pSingle[indexes[pointerIndex - 1]]);

	}

	@Override
	public void setValueForIndex(double value, int index) {
		values[index] = (int) (value);
	}

	private void reset() {
		pointerIndex = -1;
		Arrays.fill(values, -1);
	}
}