package hr.fer.zemris.edaf.statistics;

import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.genotype.Individual;
import hr.fer.zemris.edaf.genotype.binary.Binary;
import hr.fer.zemris.edaf.genotype.floatingpoint.FloatingPoint;
import hr.fer.zemris.edaf.statistics.typestat.Continuous;
import hr.fer.zemris.edaf.statistics.typestat.Discreet_binary;
import hr.fer.zemris.edaf.statistics.typestat.StatisticEngine;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * Statistics.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class Statistics {

	private StatisticEngine statisticEngine;

	public Statistics(Individual individual) {

		if (individual instanceof Binary) {

			statisticEngine = new Discreet_binary(individual);

		} else if (individual instanceof FloatingPoint) {

			statisticEngine = new Continuous(individual);

		} else {
			MSGPrinter.printERROR(System.err,
					"Statistic for " + individual.getClass()
							+ " is not supported.", true, -1);
		}
	}

	public static double getMaxValue(Individual[] samples) {
		final Individual[] copy = samples.clone();
		Arrays.sort(copy);

		return copy[copy.length - 1].getFitness();
	}

	public static double getMinValue(Individual[] samples) {
		final Individual[] copy = samples.clone();
		Arrays.sort(copy);

		return copy[0].getFitness();
	}

	public static double getAvgValue(Individual[] samples) {
		return getAvgValue(samples, 1.0);
	}

	public static double getAvgValue(Individual[] samples, double firstPerc) {
		final int index = (int) Math.floor((samples.length * firstPerc) + 0.5);

		double sum = 0;
		for (int i = 0; i < index; i++) {
			sum += samples[i].getFitness();
		}

		return sum / samples.length;
	}

	public static double getStDev(Individual[] samples) {
		final double avg = getAvgValue(samples);
		double stdev = 0;

		for (int i = 0; i < samples.length; i++) {
			stdev = (samples[i].getFitness() - avg)
					* (samples[i].getFitness() - avg);
		}
		stdev /= (samples.length - 1);

		return Math.sqrt(stdev);
	}

	public static double getMedianValue(Individual[] samples) {
		final Individual[] copy = samples.clone();
		Arrays.sort(copy);

		return copy[copy.length / 2].getFitness();
	}

	public StatisticEngine getStatisticEngine() {
		return statisticEngine;
	}

	public static String stat2String(Individual[] samples, int iter) {
		final DecimalFormat df = new DecimalFormat("0.000");
		
		return String.format("%-15s %-15s %-15s %-15s %-15s %-15s", iter,
				df.format(getMinValue(samples)),
				df.format(getMaxValue(samples)),
				df.format(getAvgValue(samples)),
				df.format(getMedianValue(samples)),
				df.format(getStDev(samples))).replaceAll(",", ".");
	}

	public static String stat2StringHeadder(String name) {
		return String.format("%-15s %-15s %-15s %-15s %-15s %-15s %-15s",
				"index", "min", "max", "avg", "med", "std", name);
	}
}