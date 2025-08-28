package hr.fer.zemris.edaf.fitness.neural;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Vector;

public class DataSet {

	Vector<double[]> inputs;
	Vector<double[]> outputs;
	
	int inputSize;
	int outputSize;
	
	
	public DataSet(int inputSize, int outputSize) {
		this.inputSize = inputSize;
		this.outputSize = outputSize;
		
		inputs = new Vector<double[]>();
		outputs = new Vector<double[]>();
	}
	
	public void loadFromFile(String path) {
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.equals("")) {
					continue;
				}
				String[] values = line.split("\\s+");
				if ( values.length != (inputSize + outputSize)) {
					System.err.println("Line " + (inputs.size() + 1) + " is not correct!");
					continue;
				}
				
				try {
					double[] inputValues = new double[inputSize];
					for (int i = 0; i < inputSize; i++) {
						inputValues[i] = Double.parseDouble(values[i]);
					}
					double[] outputValues = new double[outputSize];
					for (int i = 0; i < outputSize; i++) {
						outputValues[i] = Double.parseDouble(values[i + inputSize]);
					}
					
					inputs.add(inputValues);
					outputs.add(outputValues);
				} catch (Exception e) {
					System.err.println("Line " + (inputs.size() + 1) + " is not correct!");
					continue;
				}
			}
			br.close();
		} catch (FileNotFoundException e) {
			System.err.println("File " + path + " does nit exist!");
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		if (inputs.size() != outputs.size()) {
			throw new InvalidParameterException("File incorrect");
		}
		
	}

	public int getSize() {
		return inputs.size();
	}
	
	public double[] getInput(int i) {
		double[] d = inputs.elementAt(i);
		return d.clone();
	}
	
	public double[] getOutput(int i) {
		double[] d = outputs.elementAt(i);
		return d.clone();
	}
	
}
