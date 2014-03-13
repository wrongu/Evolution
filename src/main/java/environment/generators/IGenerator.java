package environment.generators;

public interface IGenerator {
	
	public void setSeed(long seed);
	
	public double terrainValue(double x, double y);
	
}
