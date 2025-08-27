package hr.fer.zemris.edaf.genotype.floatingpoint.crossing;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.genotype.Crossing;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.floatingpoint.FloatingPoint;

import java.util.Random;

/**
 * FLOATING POINT SIMPLE ARITHMETIC CROSSING.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class CrxSimpleArithmeticRecombination extends Crossing {

	public CrxSimpleArithmeticRecombination(double crossingProbability,
			Random rand) {
		super("CrxSimpleArithmeticRecombination", crossingProbability, rand);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Individual[] cross(Individual parent1, Individual parent2) {

		if (!(parent1 instanceof FloatingPoint)
				|| !(parent2 instanceof FloatingPoint)) {
			MSGPrinter
					.printERROR(
							System.out,
							"CrxSimpleArithmeticRecombination: individual not instance of floatingpoint.",
							true, -1);
		}

		final FloatingPoint p1 = (FloatingPoint) parent1;
		final FloatingPoint p2 = (FloatingPoint) parent2;
		final FloatingPoint c1 = new FloatingPoint(p1.getVariableNumber(),
				p1.getxMin(), p1.getxMax(), rand, p1.getPopulationLen());
		final FloatingPoint c2 = new FloatingPoint(p2.getVariableNumber(),
				p2.getxMin(), p2.getxMax(), rand, p2.getPopulationLen());

		if (rand.nextDouble() <= crossingProbability) {
			final int hiasma = rand.nextInt(p1.getVariableNumber() - 1) + 1;

			for (int i = 0; i < hiasma; i++) {
				c1.setVariable(i, p1.getVariable()[i]);
				c2.setVariable(i, p2.getVariable()[i]);
			}

			for (int i = hiasma; i < p1.getVariableNumber(); i++) {
				c1.setVariable(i, p2.getVariable()[i]);
				c2.setVariable(i, p1.getVariable()[i]);
			}
		} else {
			for (int i = 0; i < p1.getVariableNumber(); i++) {
				c1.setVariable(i, p1.getVariable()[i]);
				c2.setVariable(i, p2.getVariable()[i]);
			}
		}

		return new Individual[] { c1, c2 };

	}
}