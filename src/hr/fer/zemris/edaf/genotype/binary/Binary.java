package hr.fer.zemris.edaf.genotype.binary;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.binary.decoder.Bin2DecDecoder;
import hr.fer.zemris.edaf.statistics.reprezentation.IStatReprezentation;

import java.util.Random;

/**
 * Binary genetic algorithm.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class Binary extends Individual {

	private final byte[] bits;

	private Bin2DecDecoder decoder;

	public Binary(Bin2DecDecoder decoder, Random rand, int populationLen) {
		this.decoder = decoder;
		bits = new byte[decoder.getBitsNumber()];
		fitness = 0;
		this.rand = rand;
		variable = new double[decoder.getVariableNumber()];
		this.populationLen = populationLen;
	}

	public Bin2DecDecoder getDecoder() {
		return decoder;
	}

	public byte[] getBits() {
		return bits;
	}

	public void setBits(int index, byte value) {
		bits[index] = value;
	}

	@Override
	public Individual copy() {
		final Binary copied = new Binary(decoder, rand, populationLen);

		System.arraycopy(bits, 0, copied.bits, 0, bits.length);
		System.arraycopy(variable, 0, copied.variable, 0, variable.length);

		copied.fitness = fitness;
		return copied;
	}

	@Override
	public int getGenotypeLength() {
		return bits.length;
	}

	@Override
	public Individual[] createPopulation(boolean init) {
		final Binary[] individuals = new Binary[populationLen];
		for (int i = 0; i < populationLen; i++) {
			individuals[i] = new Binary(decoder, rand, populationLen);

			if (init) {
				initIndividual(individuals[i], null);
			}
		}
		return individuals;
	}

	public Individual[] createPopulation(double[] probabTo1) {
		final Binary[] individuals = new Binary[populationLen];
		for (int i = 0; i < populationLen; i++) {
			individuals[i] = new Binary(decoder, rand, populationLen);
			initIndividual(individuals[i], probabTo1);
		}
		return individuals;
	}

	public Individual[] createPopulation(IStatReprezentation condProbabTo1) {
		final Individual[] individuals = new Binary[populationLen];
		for (int i = 0; i < populationLen; i++) {
			individuals[i] = createIndividual(condProbabTo1);
		}
		return individuals;
	}

	public Individual createIndividual(IStatReprezentation condProbabTo1) {
		final Binary individual = new Binary(decoder, rand, populationLen);

		while (condProbabTo1.hasMoreProbabilities()) {
			final int[] indexes = condProbabTo1.getIndexesInRepresentation();

			for (int i = 0; i < indexes.length; i++) {
				final double probabilityTo1 = condProbabTo1
						.getProbability(indexes[i]);
				individual.bits[indexes[i]] = rand.nextDouble() <= probabilityTo1 ? (byte) 1
						: (byte) 0;
				condProbabTo1.setValueForIndex(individual.bits[indexes[i]],
						indexes[i]);
			}
		}

		return individual;
	}

	public Individual createIndividual(double[] probabTo1) {
		final Binary individual = new Binary(decoder, rand, populationLen);
		initIndividual(individual, probabTo1);
		return individual;
	}

	private void initIndividual(Binary individual, double[] probabTo1) {
		for (int i = 0; i < individual.bits.length; i++) {
			if (probabTo1 == null) {
				individual.bits[i] = rand.nextBoolean() ? (byte) 1 : (byte) 0;
			} else {
				individual.bits[i] = rand.nextDouble() <= probabTo1[i] ? (byte) 1
						: (byte) 0;
			}
		}
	}

	@Override
	public double[] getVariable() {
		decoder.decode(this);
		return variable;
	}

	@Override
	public void copy(Individual copy) {
		if (!(copy instanceof Binary)) {
			MSGPrinter
					.printERROR(System.err,
							"Can not non binary individual copy into binary.",
							true, -1);
		}

		final Binary b = (Binary) copy;
		System.arraycopy(b.bits, 0, bits, 0, b.bits.length);
		decoder = b.decoder;
	}

	@Override
	public int compareGenotype(Individual ind, int index) {
		final Binary b = (Binary) ind;
		if (bits[index] < b.bits[index]) {
			return -1;
		}
		if (bits[index] > b.bits[index]) {
			return 1;
		}
		return 0;
	}

	public int[] getIntBits() {
		final int[] iBits = new int[bits.length];
		for (int i = 0; i < bits.length; i++) {
			iBits[i] = bits[i];
		}
		return iBits;
	}
}