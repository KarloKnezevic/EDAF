package hr.fer.zemris.edaf.fitness.neural;

import java.security.InvalidParameterException;

public class NeuralNetworkLite implements INeuralNetwork{

	private double[] lastOutput;
	private int numberOfParameters;
	private int numberOfLayars;
	private double[] parameters;
	private int[] layers;
	private int[] nParamsPerLayer;
	
	public NeuralNetworkLite(int ... layers) {
		this.layers = layers.clone();
		this.numberOfLayars = layers.length;
		
		nParamsPerLayer = new int[layers.length];
		nParamsPerLayer[0] = 0;
		nParamsPerLayer[1] = layers[0] * 2 * layers[1];
		for (int i = 2; i < nParamsPerLayer.length; i++) {
			nParamsPerLayer[i] = (layers[i-1] + 1) * layers[i];
		}
		
		numberOfParameters = 0;
		for (int i = 0; i < nParamsPerLayer.length; i++) {
			numberOfParameters += nParamsPerLayer[i];
		}
		
		this.parameters = new double[numberOfParameters];
		for (int i = 0; i < numberOfParameters; i++) {
			this.parameters[i] = 0.4;
		}
		
		lastOutput = new double[layers[layers.length-1]];
		
	}
	
	@Override
	public double[] calcOutput(double[] d) {
		double[] output = null;
		double[] input = d;
		
		output = new double[layers[1]];
		

		for (int neuron = 0; neuron < layers[1]; neuron++) {
			int firstParameter = (nParamsPerLayer[1] / layers[1]) * neuron;
			for (int parameter = 0; parameter < input.length; parameter++) {				
				output[neuron] += 
				Math.abs(input[parameter]-parameters[firstParameter + parameter])/
						Math.abs(parameters[firstParameter + parameter+input.length]);
				
			}
			
			output[neuron] = 1/(1+output[neuron]);
		}
		
		input = output;
		
		int lastParameter = nParamsPerLayer[0] + nParamsPerLayer[1];
		for (int layer = 2; layer < layers.length; layer++) {
			output = new double[layers[layer]];
			for (int neuron = 0; neuron < layers[layer]; neuron++) {
				int firstParameter = lastParameter +
						(nParamsPerLayer[layer] / layers[layer]) * neuron;
				for(int parameter = 0; parameter < input.length; parameter++) {
					
					output[neuron] += parameters[firstParameter + parameter] * input[parameter];
				}
				output[neuron] += parameters[firstParameter + input.length];
				output[neuron] = 1/(1+Math.exp(-1*output[neuron]));
			}
			
			lastParameter += nParamsPerLayer[layer];
			input = output;
			
		}
		
		lastOutput = output;
		return output;
	}

	@Override
	public double calcError(DataSet dataSet) {
		int N = dataSet.getSize();
		double error = 0;
		for (int i = 0; i < N; i++) {
			double[] input = dataSet.getInput(i);
			double[] output = dataSet.getOutput(i);
			double[] y = calcOutput(input);
			if (y.length != output.length) {
				throw new InvalidParameterException("Invalid dataset");
			}
			
			for (int j = 0; j < output.length; j++) {
				error += (y[j] - output[j])*(y[j] - output[j]);
			}
		}
		
		error /= N;
		return error;
	}

	@Override
	public double[] getParameters() {
		
		return parameters;
	}

	@Override
	public void setParameters(double[] parameters) {
		this.parameters = parameters;		
	}

	@Override
	public double[] getLastOutput() {
		return lastOutput;
	}
	
	@Override
	public int getNumberOfParamteres() {
		return numberOfParameters;
	}
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("NEURAL NETWORK:\r\n");
		sb.append("Number of layars: ");
		sb.append(numberOfLayars);
		sb.append("\r\n");
		sb.append("Number of parameters: ");
		sb.append(numberOfParameters);
		sb.append("\r\n");
		sb.append("Parameters:\r\n");
		for (double d: getParameters()) {
			sb.append(" ");
			sb.append(d);
		}
		sb.append("\r\n");
		sb.append("Last output:\r\n");
		for (double d: lastOutput) {
			sb.append(" ");
			sb.append(d);
		}
		sb.append("\r\n");
		
		return sb.toString();
	}

	@Override
	public int test(DataSet dataSet) {
		int nFalse = 0;
		for (int i = 0; i < dataSet.getSize(); i++) {
			double[] output = dataSet.getOutput(i);
			
			String realOutput = "";
			for (int j = 0; j < output.length; j++) {
				realOutput += ((int)output[j]);
			}
		
			output = this.calcOutput(dataSet.getInput(i));
			
			String predictedOutput = "";
			for (int j = 0; j < output.length; j++) {
				
				if (output[j] < 0.5) {
					predictedOutput += "0";
				} else {
					predictedOutput += "1";
				}
			}
			
			if (!realOutput.equals(predictedOutput)) {
				nFalse++;
			}
			
			System.out.println("Real: " + realOutput + " Predicted: " + predictedOutput);
		}
		return nFalse;
	}
	
	
	public int test2(DataSet dataSet) {
		int nFalse = 0;
		for (int i = 0; i < dataSet.getSize(); i++) {
			double[] output = dataSet.getOutput(i);
			
			String realOutput = "";
			for (int j = 0; j < output.length; j++) {
				realOutput += ((int)output[j]);
			}
		
			output = this.calcOutput(dataSet.getInput(i));
			String predictedOutput = "";
			
			double max = output[0];
			int maxIndex = 0;
			
			for (int j = 1; j < output.length; j++) {
				if (output[j] > max) {
					max = output[j];
					maxIndex = j;
				}
			}
			
			switch (maxIndex) {
			case 0:
				predictedOutput = "100";
				break;
			case 1:
				predictedOutput = "010";
				break;
			case 2:
				predictedOutput = "001";
				break;

			default:
				break;
			}
			
			System.out.println("Real: " + realOutput + " Predicted: " + predictedOutput);
			
			if (!realOutput.equals(predictedOutput)) {
				nFalse++;
			}
		}
		
		return nFalse;
	}

	@Override
	public int[] getLayers() {
		return this.layers.clone();
	}

	public double[] getNeuron(int layer, int n) {
		double[] neuron = null;
		try {
			
			if (layer == 0) {
				return null;
			} else if (layer == 1) {
				neuron = new double[layers[0] * 2];
				int start = n*neuron.length;
				for (int i = 0; i < neuron.length; i++) {
					neuron[i] = parameters[start + i];
				}
			} else {
				neuron = new double[layers[layer-1] + 1];
				int start = 0;
				for (int i = 1; i < layer; i++) {
					start += nParamsPerLayer[i];
				}
				start += n*neuron.length;
				for (int i = 0; i < neuron.length; i++) {
					neuron[i] = parameters[start + i];
				}
			}
		} catch (Exception e) {
			return null;
		}
		
		return neuron;
	}
	

}
