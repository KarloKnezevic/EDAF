package hr.fer.zemris.edaf.algorithm;

import hr.fer.zemris.edaf.IFrameworkContext;
import hr.fer.zemris.edaf.MSGPrinter;
import hr.fer.zemris.edaf.algorithm.eda.dependent.BMDA;
import hr.fer.zemris.edaf.algorithm.eda.dependent.MIMIC;
import hr.fer.zemris.edaf.algorithm.eda.independent.CGA;
import hr.fer.zemris.edaf.algorithm.eda.independent.PBIL;
import hr.fer.zemris.edaf.algorithm.eda.independent.UMDA;
import hr.fer.zemris.edaf.algorithm.ga.eliminative.GAe;
import hr.fer.zemris.edaf.algorithm.ga.generational.GAg;
import hr.fer.zemris.edaf.algorithm.hybrid.HybridEda;
import hr.fer.zemris.edaf.genotype.Genotype;
import hr.fer.zemris.edaf.selection.Selection;
import hr.fer.zemris.edaf.workenvironment.Evaluation;

/**
 * AlgorithmFactory.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class AlgorithmFactory {

	private Algorithm algorithm;

	private final IFrameworkContext context;

	private final Genotype genotype;

	private final Selection selection;

	private final Evaluation evaluation;

	public AlgorithmFactory(IFrameworkContext context, Genotype genotype,
			Selection selection, Evaluation evaluation) {

		this.context = context;
		this.genotype = genotype;
		this.selection = selection;
		this.evaluation = evaluation;

		initialize();

	}

	private void initialize() {

		if (context.getAlgorithmName().indexOf("-") > 0) {
			final String[] algs = context.getAlgorithmName().split("-");
			algorithm = getHybridEda(init(algs[0]), init(algs[1]));
		} else {
			algorithm = init(context.getAlgorithmName());
		}

		algorithm.setLogFreq(context.getLogFrequency());
		algorithm.setDestValue(context.getDestValue());

	}

	public Algorithm getAlgorithm() {
		return algorithm;
	}

	private Algorithm init(String algorithmName) {

		switch (algorithmName.toLowerCase()) {
		case "gag":
			return getGaG();
		case "gae":
			return getGaE();
		case "cga":
			return getCga();
		case "umda":
			return getUmda();
		case "pbil":
			return getPbil();
		case "mimic":
			return getMimic();
		case "bmda":
			return getBmda();
		}

		MSGPrinter.printERROR(System.err, "Unsupported algorithm.", true, -1);

		return null;
	}

	private GAg getGaG() {

		return new GAg(context.getRand(), genotype, selection, evaluation,
				context.getMaxNumberOfGen(), context.getStagnation(),
				context.getElitism());
	}

	private GAe getGaE() {

		return new GAe(context.getRand(), genotype, selection, evaluation,
				context.getMaxNumberOfGen(), context.getStagnation(),
				context.getMortality());
	}

	private CGA getCga() {

		return new CGA(context.getRand(), genotype, selection, evaluation,
				context.getMaxNumberOfGen(), context.getStagnation(), 0);
	}

	private UMDA getUmda() {

		return new UMDA(context.getRand(), genotype, selection, evaluation,
				context.getMaxNumberOfGen(), context.getStagnation(),
				context.getElitism(), context.getEstimationProbability());
	}

	private PBIL getPbil() {

		return new PBIL(context.getRand(), genotype, selection, evaluation,
				context.getMaxNumberOfGen(), context.getStagnation(),
				context.getElitism(), context.getEstimationProbability());
	}

	private HybridEda getHybridEda(Algorithm algorithm1, Algorithm algorithm2) {

		return new HybridEda(context.getRand(), genotype, selection,
				evaluation, context.getMaxNumberOfGen(),
				context.getStagnation(), context.getElitism(), algorithm1,
				algorithm2, context.getRatioSelector(),
				context.getRatioSelectorRatio());
	}

	private MIMIC getMimic() {

		return new MIMIC(context.getRand(), genotype, selection, evaluation,
				context.getMaxNumberOfGen(), context.getStagnation(),
				context.getElitism(), context.getEstimationProbability());
	}

	private BMDA getBmda() {

		return new BMDA(context.getRand(), genotype, selection, evaluation,
				context.getMaxNumberOfGen(), context.getStagnation(),
				context.getElitism(), context.getEstimationProbability());
	}

}