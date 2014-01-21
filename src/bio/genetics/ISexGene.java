package bio.genetics;

public interface ISexGene<T> extends IGene<T> {
	
	public boolean isCompatible(ISexGene<T> other);
	
	public ISexGene<T> cross(ISexGene<T> other, int minblock, int maxblock) throws IncompatibleParentsException;

}
