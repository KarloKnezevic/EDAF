package hr.fer.zemris.edaf.genotype.floatingpoint.mutation;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.Mutation;
import hr.fer.zemris.edaf.genotype.floatingpoint.FloatingPoint;

import java.util.Random;

/**
 * FLOATING POINT SIMPLE MUTATION
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class MutSimpleFP extends Mutation {

	public MutSimpleFP(double mutationProbability, Random rand) {
		super("MutSimpleFP", mutationProbability, rand);
	}

	@Override
	public void mutate(Individual individual) {

		if (!(individual instanceof FloatingPoint)) {
			MSGPrinter.printERROR(System.out,
					"CrxSimple: individual not instance of floatingpoint.",
					true, -1);
		}

		final FloatingPoint fp = (FloatingPoint) individual;

		for (int i = 0; i < fp.getVariableNumber(); i++) {
			if (rand.nextDouble() <= mutationProbability) {
				fp.setVariable(i, fp.getxMin()
						+ ((fp.getxMax() - fp.getxMin()) * rand.nextDouble()));
			}
		}
	}
}