package hr.fer.zemris.edaf.workenvironment.boolSBox;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.algorithm.Algorithm;
import hr.fer.zemris.edaf.fitness.bool.BooleanFunctionOptimization;
import hr.fer.zemris.edaf.fitness.bool.IBool;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.binary.Binary;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

/**
 * TEST BBOB Expertiments for Master Thesis.
 * 
 * Function: bool Dimension: 1, 256 bits
 * 
 * Test bool operations [y-yes | n - no]: 1. BALANISRANOST, y 2. NELINEARNOST, y
 * 3. KORELACIJSKI IMUNITET, y 4. WALSHOV SPEKTAR, y 5. ALGEBARSKI STUPANJ, y 6.
 * ALGEBARSKI IMUNITET, y 7. KARAKTERISTIKA PROPAGACIJE, y 8. SUMA KVADRATA
 * INDIKATOR, y 9. AC, y
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class TEST extends Evaluation {

	private IBool bool;

	public TEST(String[] args) {
		super(args);
	}

	@Override
	public void init(Algorithm algorithm) {

		if (!(algorithm.getGenotype().getIndividual() instanceof Binary)) {
			MSGPrinter.printERROR(System.err,
					"Boolean functions only for binary genotype", true, -1);
		}

		final Binary b = (Binary) algorithm.getGenotype().getIndividual();

		final int n = (int) (Math.log(b.getGenotypeLength()) / Math.log(2));

		if (Math.pow(2, n) != b.getGenotypeLength()) {
			MSGPrinter.printERROR(System.err,
					"Genotype length must be potention of 2", true, -1);
		}

		// 1-y | 0-n
		bool = new BooleanFunctionOptimization(n, new int[] { 1, 1, 1, 1, 1, 1,
				1, 1, 1 });

		algorithm.run();

	}

	@Override
	public void evaluate(Individual individual) {

		if (!(individual instanceof Binary)) {
			MSGPrinter.printERROR(System.err,
					"Boolean functions only for binary genotype", true, -1);
		}

		final Binary b = (Binary) individual;

		individual.setFitness(-1 * bool.computeBool(b.getIntBits()));

	}

}
