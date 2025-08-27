package hr.fer.zemris.edaf.genotype.binary.decoder;

import hr.fer.zemris.edaf.genotype.binary.Binary;

/**
 * Binary number to decade decoder.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class BinaryDecoder extends Bin2DecDecoder {

	public BinaryDecoder(int variableNumber, int precision, double xMin,
			double xMax, boolean bitsNumberSetting) {

		super(variableNumber, precision, xMin, xMax, bitsNumberSetting);

	}

	@Override
	public void decode(Binary b) {

		int bitIndex = 0;

		for (int variable = 0; variable < variableNumber; variable++) {

			final int firstBit = bitIndex;
			final int lastBit = (firstBit + bitsPerVariable[variable]) - 1;
			bitIndex += bitsPerVariable[variable];

			int binary = 0;
			for (int i = firstBit; i <= lastBit; i++) {
				binary = binary * 2;
				if (b.getBits()[i] == 1) {
					binary = binary + 1;
				}
			}

			final double variableValue = (((double) binary / (double) maxBinaryPerVariable[variable]) * (xMax[variable] - xMin[variable]))
					+ xMin[variable];

			b.setVariable(variable, variableValue);
		}
	}
}