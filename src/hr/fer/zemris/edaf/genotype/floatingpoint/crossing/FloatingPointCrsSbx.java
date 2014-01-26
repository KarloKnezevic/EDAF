package hr.fer.zemris.edaf.genotype.floatingpoint.crossing;

import java.util.Random;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.genotype.Crossing;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.floatingpoint.FloatingPoint;

public class FloatingPointCrsSbx extends Crossing {
	
	//algorithm parameter
	private int ni;

	public FloatingPointCrsSbx(Random rand, int ni) {
		super("sbx", 0, rand);
		
		this.ni = ni;
	}

	@Override
	public Individual[] cross(Individual parent1, Individual parent2) {
		
		if (!(parent1 instanceof FloatingPoint)
				|| !(parent2 instanceof FloatingPoint)) {
			MSGPrinter
					.printERROR(
							System.out,
							"FloatingPointCrsSbx: individual not instance of floatingpoint.",
							true, -1);
		}
		
		//in one crossing made 2 children
		return new Individual[] { 
				createChild (parent1, parent2), 
				createChild (parent2, parent1) 
				};
	
	}
	
	private FloatingPoint createChild (Individual parent1, Individual parent2) {
		
		final FloatingPoint p1 = (FloatingPoint) parent1;
		final FloatingPoint p2 = (FloatingPoint) parent2;
		
		final FloatingPoint ch = new FloatingPoint(p1.getVariableNumber(),
				p1.getxMin(), p1.getxMax(), rand, p1.getPopulationLen());
		
		// repeat for each variable
		for (int i = 0; i < p1.getVariableNumber(); i++) {
			double p1x = p1.getVariable()[i];
			double p2x = p2.getVariable()[i];
			
			// determine smaller and greater parent values
			double low = p1x, high = p2x;
			if (p2x < p1x) {
				low = p2x;
				high = p1x;
			}
			
			// check for close or same parents
			if (Math.abs(high - low) < 1.e-12) {
				ch.setVariable(i, (high + low) / 2);
				continue;
			}
			
			// determine min[(low - LBound), (UBound - high)]
			double min = low - p1.getxMin();
			if ((p1.getxMax() - high) < min) {
				min = p1.getxMax() - high;
			}
			
			// determine beta and alpha
			double beta = 1 + 2 * min / (high - low);
			double alpha = 2 - 1 / Math.pow(beta, 1. + ni);
			
			double u = rand.nextDouble();
			// scale down to avoid u == 1
			u *= 0.999;
			
			double beta_dash;
			// if u is smaller than 1/alpha perform a contracting crossover
			if (u <= (1. / alpha)) {
				beta_dash = Math.pow(alpha * u, 1.0 / (ni + 1.0));
			}
			// otherwise perform an expanding crossover
			else {
				beta_dash = Math.pow(1.0 / (2.0 - alpha * u), 1.0 / (ni + 1.0));
			}
			
			// apply beta_dash
			if (rand.nextBoolean()) {
				ch.setVariable(i, 
						(p1.getVariable()[i] + p2.getVariable()[i])/2.0 - 
						beta_dash*0.5*Math.abs(p1.getVariable()[i] - p2.getVariable()[i])
						);
			} else {
				ch.setVariable(i, 
						(p1.getVariable()[i] + p2.getVariable()[i])/2.0 + 
						beta_dash*0.5*Math.abs(p1.getVariable()[i] - p2.getVariable()[i])
						);
			}
		}
		
		return ch;
	}

}
