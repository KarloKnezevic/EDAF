package hr.fer.zemris.edaf.genotype.binary.decoder;

import hr.fer.zemris.edaf.genotype.binary.Binary;

/**
 * Gray number to deacde decoder.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class GrayDecoder extends Bin2DecDecoder {

	public GrayDecoder(int variableNumber, int precision, double xMin,
			double xMax, boolean bitsNumberSetting) {

		super(variableNumber, precision, xMin, xMax, bitsNumberSetting);

	}

	/**
	 * Converting gray code to decade. There are 2 steps in decoding: 1. gray ->
	 * binary 2. binary -> decade Algorithm for converting gray to binary: a)
	 * MSB gray bit is same as MSB binary (go left to right in algorithm) b)
	 * value of i-th binary bit is: b1) if i-th gray bit is 1, then complement
	 * (i-1)- binary bit else transcribe (i-1) binary bit c) repeat b) until
	 * end.
	 */
	@Override
	public void decode(Binary b) {

		int bitIndex = 0;

		for (int variable = 0; variable < variableNumber; variable++) {

			final int firstBit = bitIndex;
			final int lastBit = (firstBit + bitsPerVariable[variable]) - 1;
			bitIndex += bitsPerVariable[variable];

			int binary = 0;
			int g2b = b.getBits()[firstBit];
			for (int i = firstBit; i <= lastBit; i++) {
				binary = binary * 2;
				if (g2b == 1) {
					binary = binary + 1;
				}

				if ((i + 1) <= lastBit) {
					g2b = b.getBits()[i + 1] == 1 ? (1 - g2b) : g2b;
				}

			}

			final double variableValue = (((double) binary / (double) maxBinaryPerVariable[variable]) * (xMax[variable] - xMin[variable]))
					+ xMin[variable];

			b.setVariable(variable, variableValue);
		}
	}
}