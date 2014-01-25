package bio.genetics;

public abstract class SexGene<T> extends Gene<T> {
	
	public abstract boolean isCompatible(SexGene<T> other);
	
	public abstract SexGene<T> cross(SexGene<T> other, int minblock, int maxblock) throws IncompatibleParentsException;

}
