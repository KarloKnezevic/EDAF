package hr.fer.zemris.edaf.genotype;

import hr.fer.zemris.edaf.IFrameworkContext;
import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.genotype.binary.Binary;
import hr.fer.zemris.edaf.genotype.binary.crossing.CrxOnePoint;
import hr.fer.zemris.edaf.genotype.binary.decoder.Bin2DecDecoder;
import hr.fer.zemris.edaf.genotype.binary.decoder.BinaryDecoder;
import hr.fer.zemris.edaf.genotype.binary.decoder.GrayDecoder;
import hr.fer.zemris.edaf.genotype.binary.mutation.MutSimple;
import hr.fer.zemris.edaf.genotype.floatingpoint.FloatingPoint;
import hr.fer.zemris.edaf.genotype.floatingpoint.crossing.CrxSimpleArithmeticRecombination;
import hr.fer.zemris.edaf.genotype.floatingpoint.mutation.MutSimpleFP;

/**
 * Genotype Factory
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class GenotypeFactory {

	private final Genotype genotype;

	private Individual individual;

	private Mutation mutation;

	private Crossing crossing;

	public GenotypeFactory(IFrameworkContext context) {

		initialize(context);

		genotype = new Genotype(mutation, crossing, individual);

	}

	public Genotype getGenotype() {
		return genotype;
	}

	private void initialize(IFrameworkContext context) {

		if (context.getGenotype().equals("B")) {
			initializeBinary(context);
		} else if (context.getGenotype().equals("FP")) {
			initializeFloatingPoint(context);
		} else {
			MSGPrinter
					.printERROR(System.err, "Unsupported genotype.", true, -1);
		}

	}

	private void initializeBinary(IFrameworkContext context) {

		Bin2DecDecoder decoder = null;
		boolean bitsNumberSetting = false;

		if (context.getPrecisionDescription().equals("bits")) {
			bitsNumberSetting = true;
		} else if (context.getPrecisionDescription().equals("decimal")) {
			bitsNumberSetting = false;
		} else {
			MSGPrinter.printERROR(System.err,
					"Unsupported binary precision description type.", true, -1);
		}

		if (context.getEncoding().equals("binary")) {
			decoder = new BinaryDecoder(context.getDimension(),
					context.getPrecision(), context.getLBound(),
					context.getUBound(), bitsNumberSetting);
		} else if (context.getEncoding().equals("gray")) {
			decoder = new GrayDecoder(context.getDimension(),
					context.getPrecision(), context.getLBound(),
					context.getUBound(), bitsNumberSetting);
		} else {
			MSGPrinter.printERROR(System.err,
					"Unsupported binary encoding type.", true, -1);
		}

		individual = new Binary(decoder, context.getRand(),
				context.getPopulationSize());

		if (context.getMutation().equals("simple")) {
			mutation = new MutSimple(context.getMutationProb(),
					context.getRand());
		} else {
			MSGPrinter.printERROR(System.err,
					"Unsupported binary mutation type.", true, -1);
		}

		if (context.getCrossing().equals("onePoint")) {
			crossing = new CrxOnePoint(context.getCrossingProb(),
					context.getRand());
		} else {
			MSGPrinter.printERROR(System.err,
					"Unsupported binary crossing type.", true, -1);
		}

	}

	private void initializeFloatingPoint(IFrameworkContext context) {

		individual = new FloatingPoint(context.getDimension(),
				context.getLBound(), context.getUBound(), context.getRand(),
				context.getPopulationSize());

		if (context.getMutation().equals("simple")) {
			mutation = new MutSimpleFP(context.getMutationProb(),
					context.getRand());
		} else {
			MSGPrinter.printERROR(System.err,
					"Unsupported floating point mutation type.", true, -1);
		}

		if (context.getCrossing().equals("simpleArithmetic")) {
			crossing = new CrxSimpleArithmeticRecombination(
					context.getCrossingProb(), context.getRand());
		} else {
			MSGPrinter.printERROR(System.err,
					"Unsupported floating point crossing type.", true, -1);
		}
	}
}