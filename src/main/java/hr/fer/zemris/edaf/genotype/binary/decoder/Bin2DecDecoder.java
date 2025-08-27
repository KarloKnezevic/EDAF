package hr.fer.zemris.edaf.genotype.binary.decoder;

import hr.fer.zemris.edaf.genotype.binary.Binary;

/**
 * Binary to decade decoder.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public abstract class Bin2DecDecoder {

	protected double[] xMin;

	protected double[] xMax;

	protected int precision;

	protected int variableNumber;

	protected int bitsNumber;

	protected int[] bitsPerVariable;

	protected int[] maxBinaryPerVariable;

	public Bin2DecDecoder(int variableNumber, int precision, double xMin,
			double xMax, boolean bitsNumberSetting) {

		this.precision = precision;
		this.variableNumber = variableNumber;
		this.xMin = new double[variableNumber];
		this.xMax = new double[variableNumber];

		if (bitsNumberSetting) {
			bitsNumber = precision;
		} else {
			bitsNumber = countBitsForPrecision(precision, xMin, xMax);
		}

		bitsPerVariable = new int[variableNumber];
		maxBinaryPerVariable = new int[variableNumber];

		for (int i = 0; i < variableNumber; i++) {
			this.xMin[i] = xMin;
			this.xMax[i] = xMax;
			bitsPerVariable[i] = bitsNumber;
			maxBinaryPerVariable[i] = (1 << bitsNumber) - 1;
		}

		bitsNumber = bitsNumber * variableNumber;

	}

	public abstract void decode(Binary b);

	public int getBitsNumber() {
		return bitsNumber;
	}

	public int getVariableNumber() {
		return variableNumber;
	}

	private int countBitsForPrecision(int precision, double xMin, double xMax) {

		if (xMin == xMax) {
			return 0;
		}
		final double numerator = Math.log((xMax - xMin)
				/ Math.pow(10, -1 * precision));
		return (int) Math.ceil(numerator / Math.log(2));

	}

}