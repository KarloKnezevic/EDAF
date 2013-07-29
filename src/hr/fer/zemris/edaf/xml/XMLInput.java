package hr.fer.zemris.edaf.xml;

import java.io.OutputStream;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

/**
 * XMLInput. EXAMPLE FILE. NOT FINISHED.
 * 
 * @author Karlo Knezevic, karlo.knezevic@fer.hr
 * 
 */
public class XMLInput {

	private final String[] section1 = { "Algorithm", "Name" };

	private final String[] section1Val = { "ga|pbil|umda|cga|mimic|"
			+ "ga-umda|ga-pbil|umda-pbil" };

	private final String[] section2 = { "Genotype", "Gene", "lBound", "uBound",
			"Dimension", "Precision", "Crossing", "Mutation" };

	private final String[] section2Val = { "B|FP", "-Inf", "Inf",
			"Positive Integer", "Positive Integer", "[0,1]", "[0,1]" };

	private final String[] section3 = { "Registry", "PopulationSize",
			"EstimationProbability", "Elitism", "MaxNumberOfGen", "Stagnation",
			"LogFile", "Results", "Selection" };

	private final String[] section3Val = { "Positive Integer", "[0,1]",
			"Positive Integer", "Positive Integer", "Positive Integer", "Path",
			"Path", "firstBest|tournament|proportional" };

	private final String[] section4 = { "Test", "RunNumber" };

	private final String[] section4Val = { "Positive Integer" };

	private final String[] section5 = { "ParamTest", "Param", "lValue", "Step",
			"uValue" };

	private final String[] section5Val = { "-", "Double", "Double", "Double" };

	public void generate(OutputStream os) {

		final Element root = new Element("EDAF");

		final Element s1 = makeElement(section1, section1Val);
		final Element s2 = makeElement(section2, section2Val);
		final Element s3 = makeElement(section3, section3Val);
		final Element s4 = makeElement(section4, section4Val);
		final Element s5 = makeElement(section5, section5Val);

		root.appendChild(s1);
		root.appendChild(s2);
		root.appendChild(s3);
		root.appendChild(s4);
		root.appendChild(s5);

		final Document doc = new Document(root);

		try {
			format(os, doc);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private Element makeElement(String[] keys, String[] values) {
		final Element root = new Element(keys[0]);

		for (int i = 0; i < values.length; i++) {
			final Element e = new Element(keys[i + 1]);
			e.appendChild(values[i]);
			root.appendChild(e);
		}

		return root;
	}

	private void format(OutputStream os, Document doc) throws Exception {
		final Serializer serializer = new Serializer(os, "UTF-8");
		serializer.setIndent(4);
		serializer.setMaxLength(60);
		serializer.write(doc);
		serializer.flush();
	}

}