package hr.fer.zemris.edaf.fitness.deceptive;

/**
 * Bit Len Trap Ones Deception function. Minimum is achieved when all bits are 1
 * and maximum iff one bit is zerro.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class BitLenTrapOnesDeception implements IDeceptive {

	@Override
	public double computeDeceptive(byte[] bits) {
		int oneSum = 0;

		for (int i = 0; i < bits.length; i++) {
			if (bits[i] == 1) {
				oneSum++;
			}
		}

		if (bits.length == oneSum) {
			return -1 * oneSum;
		}

		return -1 * (bits.length - 1 - oneSum);
	}

}