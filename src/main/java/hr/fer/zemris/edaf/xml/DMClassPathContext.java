package hr.fer.zemris.edaf.xml;

public class DMClassPathContext extends ClassPathFrameworkContext {
	
	private int precision;

	public DMClassPathContext(String xml) {
		super(xml);
	}
	
	@Override
	public int getPrecision() {
		return precision;
	}
	
	public void setPrecision(int precision) {
		this.precision = precision;
	}

}
