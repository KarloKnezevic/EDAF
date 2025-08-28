package hr.fer.zemris.edaf.fitness.neural;

public interface INeuralNetwork {
	
	double[] calcOutput(double d[]);
	double calcError(DataSet dataSet);
	double[] getParameters();
	void setParameters(double[] parameters);
	double[] getLastOutput();
	int getNumberOfParamteres();
	int[] getLayers();
	
	
	int test(DataSet dataSet);
	int test2(DataSet dataSet);

}
