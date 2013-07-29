package hr.fer.zemris.edaf.fitness.deceptive;

/**
 * One max deceptive function. Fitness is higher if number of ones is higher.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class OneMax implements IDeceptive {

	@Override
	public double computeDeceptive(byte[] bits) {
		int oneSum = 0;

		for (int i = 0; i < bits.length; i++) {
			if (bits[i] == 1) {
				oneSum++;
			}
		}

		return -1 * oneSum;
	}

}
