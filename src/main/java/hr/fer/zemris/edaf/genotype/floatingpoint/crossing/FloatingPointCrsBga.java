package hr.fer.zemris.edaf.genotype.floatingpoint.crossing;

import java.util.Random;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.genotype.Crossing;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.floatingpoint.FloatingPoint;

public class FloatingPointCrsBga extends Crossing {

	public FloatingPointCrsBga(Random rand) {
		super("bga", 0, rand);
	}

	@Override
	public Individual[] cross(Individual parent1, Individual parent2) {
		
		if (!(parent1 instanceof FloatingPoint)
				|| !(parent2 instanceof FloatingPoint)) {
			MSGPrinter
					.printERROR(
							System.out,
							"FloatingPointCrsBga: individual not instance of floatingpoint.",
							true, -1);
		}
		
		//in one crossing made 2 children
		return new Individual[] { 
				createChild (parent1, parent2), 
				createChild (parent2, parent1) 
				};
		
	}
	
	/**
	 * Faculty of Electrical Engineering and Computing, University of Zagreb
	 * Method source by Domagoj Jakobovic, PhD
	 * @param parent1 first parent
	 * @param parent2 second parent
	 * @return child
	 */
	private FloatingPoint createChild (Individual parent1, Individual parent2) {
		
		final FloatingPoint p1 = (FloatingPoint) parent1;
		final FloatingPoint p2 = (FloatingPoint) parent2;
		
		final FloatingPoint ch = new FloatingPoint(p1.getVariableNumber(),
				p1.getxMin(), p1.getxMax(), rand, p1.getPopulationLen());
		
		int a;
		int size = p1.getGenotypeLength();
		
		//upper - lower bound
		double range = 0.5 * (p1.getxMax() - p1.getxMin());
		double gama = 0, lambda = 0, b;
		
		for (int i = 0; i <= 15; i++) {
			//0,1,...15
			a = rand.nextInt(16);
			
			// 1/16 probability for 1 = 1
			a = a == 15 ? 1 : 0;
			gama += a*Math.pow(2.0, -i);
		}
		
		double norm = 0;
		for (int i = 0; i < size; i++) {
			norm += (p1.getVariable()[i] - p2.getVariable()[i]) * 
					(p1.getVariable()[i] - p2.getVariable()[i]);
		}
		norm = Math.sqrt(norm);
		
		// scaling safeguard 
		if(norm < 10e-9) {
			norm = 1;
		}
		
		// determine better parent
		// better parent is parent with smaller fitness
		// this framework is currently developed for minimization
		FloatingPoint better, worse;
		if (p1.getFitness() < p2.getFitness()) {
			better = p1;
			worse = p2;
		} else {
			better = p2;
			worse = p1;
		}
		
		//build child
		for (int i = 0; i < size; i++) {
			// worse gene minus better gene divided with norm
			lambda = (worse.getVariable()[i] - better.getVariable()[i]) / norm;
			
			b = rand.nextDouble();
			// minus with probability 0.9
			ch.setVariable(i, b <= 0.9 ? 
					better.getVariable()[i] - range*gama*lambda :
						better.getVariable()[i] + range*gama*lambda);
			
			// check for bounds; if outside the interval, bring closer to better parent
			if (ch.getVariable()[i] > ch.getxMax()) {
				ch.setVariable(i, 
						rand.nextDouble()*(ch.getxMax() - better.getVariable()[i]));
			} else if (ch.getVariable()[i] < ch.getxMin()) {
				ch.setVariable(i, 
						rand.nextDouble()*(better.getVariable()[i] - ch.getxMin()));
			}
		}
			
		return ch;
		
	}

}
