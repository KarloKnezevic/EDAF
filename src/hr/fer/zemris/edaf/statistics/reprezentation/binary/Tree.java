package hr.fer.zemris.edaf.statistics.reprezentation.binary;

import hr.fer.zemris.edaf.statistics.reprezentation.IStatReprezentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TREE REPRESENTATION
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class Tree implements IStatReprezentation {

	// key is child index and value is parent index
	Map<Integer, Integer> tree;

	/**
	 * Single probabilities of elements.
	 */
	private double[] pSingle;

	/**
	 * Joint probability. i-th place is joint probability of (i-th | front of
	 * i-th in indexes).
	 */
	private final double[] pJoint;

	private final int[] values;

	private int[] prnt;
	private boolean frst;

	public Tree(int samples) {
		pJoint = new double[samples];
		values = new int[samples];
		tree = new HashMap<>();
		frst = true;
	}

	public void setPSingle(double[] pSingle) {
		this.pSingle = pSingle;
	}

	public void set(int childIndex, int parentIndex, double joint) {
		pJoint[childIndex] = joint;
		tree.put(childIndex, parentIndex);
	}

	public void setIndependent(int index) {
		pJoint[index] = 0;
		tree.put(index, -1);
	}

	public int getParent(int childIndex) {
		if (!tree.containsKey(childIndex)) {
			return -1;
		}
		return tree.get(childIndex).intValue();
	}

	@Override
	public double getLength() {
		hasMoreProbabilities();
		return prnt.length;
	}

	@Override
	public boolean hasMoreProbabilities() {
		List<Integer> indexList = new ArrayList<>();

		if (frst) {
			indexList = getChildrenForParent(-1);
			frst = false;
		} else {
			for (int i = 0; i < prnt.length; i++) {
				final List<Integer> list = getChildrenForParent(prnt[i]);
				if (list != null) {
					indexList.addAll(list);
				}
			}
		}

		prnt = new int[indexList.size()];
		for (int i = 0; i < prnt.length; i++) {
			prnt[i] = indexList.get(i).intValue();
		}

		if (!indexList.isEmpty()) {
			return true;
		}

		reset();
		return false;
	}

	@Override
	public int[] getIndexesInRepresentation() {
		return prnt;
	}

	@Override
	public double getProbability(int index) {
		if (tree.get(index).intValue() == -1) {
			return pSingle[index];
		}

		return values[tree.get(index).intValue()] == 1 ? pJoint[index]
				/ pSingle[tree.get(index).intValue()] : pJoint[index]
				/ (1.0 - pSingle[tree.get(index).intValue()]);
	}

	@Override
	public void setValueForIndex(double value, int index) {
		values[index] = (int) (value);
	}

	private void reset() {
		Arrays.fill(values, -1);
		frst = true;
	}

	private List<Integer> getChildrenForParent(int parentIndex) {
		final List<Integer> list = new ArrayList<>();
		for (final Map.Entry<Integer, Integer> entry : tree.entrySet()) {
			if (entry.getValue().intValue() != parentIndex) {
				continue;
			}
			list.add(entry.getKey());
		}

		return list;
	}

}
