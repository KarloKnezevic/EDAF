package hr.fer.zemris.edaf.xml;

import hr.fer.zemris.edaf.IFrameworkContext;
import hr.fer.zemris.edaf.MSGPrinter;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

/**
 * ClassPathFrameworkContext.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class ClassPathFrameworkContext implements IFrameworkContext {

	private Document xmlDoc;

	private final Element root;

	private final Random rand;

	/**
	 * Path to XML configuration file.
	 * 
	 * @param xml
	 */
	public ClassPathFrameworkContext(String xml) {

		final File file = new File(xml);

		if (!file.isFile() || !file.canRead()) {
			MSGPrinter.printERROR(System.err, xml
					+ " is not valid XML file or valid path.", true, -1);
		}

		try {
			xmlDoc = new Builder().build(file);
		} catch (final ValidityException e) {
			MSGPrinter.printERROR(System.err, e.toString(), true, -1);
		} catch (final ParsingException e) {
			MSGPrinter.printERROR(System.err, e.toString(), true, -1);
		} catch (final IOException e) {
			MSGPrinter.printERROR(System.err, e.toString(), true, -1);
		}

		root = xmlDoc.getRootElement();

		rand = new Random();

	}

	@Override
	public String getAlgorithmName() {

		return root.getChildElements("Algorithm").get(0)
				.getChildElements("Name").get(0).getValue();

	}

	@Override
	public String getWorkEnvironment() {

		return root.getChildElements("WorkEnvironment").get(0)
				.getChildElements("Name").get(0).getValue();

	}

	@Override
	public String getGenotype() {

		return root.getChildElements("Genotype").get(0)
				.getChildElements("Gene").get(0).getValue();

	}

	@Override
	public String getEncoding() {

		return root.getChildElements("Genotype").get(0)
				.getChildElements("Encoding").get(0).getValue();

	}

	@Override
	public int getLBound() {

		return Integer.parseInt(root.getChildElements("Genotype").get(0)
				.getChildElements("lBound").get(0).getValue());

	}

	@Override
	public int getUBound() {

		return Integer.parseInt(root.getChildElements("Genotype").get(0)
				.getChildElements("uBound").get(0).getValue());

	}

	@Override
	public int getDimension() {

		return Integer.parseInt(root.getChildElements("Genotype").get(0)
				.getChildElements("Dimension").get(0).getValue());

	}

	@Override
	public int getPrecision() {

		return Integer.parseInt(root.getChildElements("Genotype").get(0)
				.getChildElements("Precision").get(0).getValue());

	}

	@Override
	public String getPrecisionDescription() {
		return root.getChildElements("Genotype").get(0)
				.getChildElements("Precision").get(0).getAttribute("key")
				.getValue();
	}

	@Override
	public String getCrossing() {

		return root.getChildElements("Genotype").get(0)
				.getChildElements("Crossing").get(0).getAttribute("key")
				.getValue();

	}
	
	@Override
	public int getNi() {
		return Integer.parseInt(root.getChildElements("Genotype").get(0)
				.getChildElements("Crossing").get(0).getAttribute("ni")
				.getValue());
	}

	@Override
	public double getCrossingProb() {

		return Double.parseDouble(root.getChildElements("Genotype").get(0)
				.getChildElements("Crossing").get(0).getValue());

	}

	@Override
	public String getMutation() {

		return root.getChildElements("Genotype").get(0)
				.getChildElements("Mutation").get(0).getAttribute("key")
				.getValue();

	}

	@Override
	public double getMutationProb() {

		return Double.parseDouble(root.getChildElements("Genotype").get(0)
				.getChildElements("Mutation").get(0).getValue());

	}

	@Override
	public int getPopulationSize() {

		return Integer.parseInt(root.getChildElements("Registry").get(0)
				.getChildElements("PopulationSize").get(0).getValue());

	}

	@Override
	public double getEstimationProbability() {

		return Double.parseDouble(root.getChildElements("Registry").get(0)
				.getChildElements("EstimationProbability").get(0).getValue());

	}

	@Override
	public int getElitism() {

		return Integer.parseInt(root.getChildElements("Registry").get(0)
				.getChildElements("Elitism").get(0).getValue());

	}

	@Override
	public double getMortality() {

		return Double.parseDouble(root.getChildElements("Registry").get(0)
				.getChildElements("Mortality").get(0).getValue());

	}

	@Override
	public int getMaxNumberOfGen() {

		return Integer.parseInt(root.getChildElements("Registry").get(0)
				.getChildElements("MaxNumberOfGen").get(0).getValue());

	}

	@Override
	public int getStagnation() {

		return Integer.parseInt(root.getChildElements("Registry").get(0)
				.getChildElements("Stagnation").get(0).getValue());

	}

	@Override
	public int getLogFrequency() {

		return Integer.parseInt(root.getChildElements("Registry").get(0)
				.getChildElements("LogFrequency").get(0).getValue());

	}

	@Override
	public String getLogDirectory() {

		return root.getChildElements("Registry").get(0)
				.getChildElements("LogDirectory").get(0).getValue();

	}

	@Override
	public String getSelection() {

		return root.getChildElements("Registry").get(0)
				.getChildElements("Selection").get(0).getAttribute("key")
				.getValue();

	}

	@Override
	public double getSelectionRatio() {

		return Double.parseDouble(root.getChildElements("Registry").get(0)
				.getChildElements("Selection").get(0).getAttribute("ratio")
				.getValue());

	}

	@Override
	public double getSelectionParam() {

		return Double.parseDouble(root.getChildElements("Registry").get(0)
				.getChildElements("Selection").get(0).getValue());

	}

	@Override
	public Random getRand() {

		return rand;

	}

	@Override
	public String getRatioSelector() {
		return root.getChildElements("AlgorithmParam").get(0)
				.getChildElements("RatioSelector").get(0).getValue();
	}

	@Override
	public double getRatioSelectorRatio() {
		return Double.parseDouble(root.getChildElements("AlgorithmParam")
				.get(0).getChildElements("RatioSelector").get(0)
				.getAttribute("ratio").getValue());
	}

}