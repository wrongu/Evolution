package bio.genetics;

import environment.Environment;

public interface IGene<T> {

	public IGene<T> mutate(double rate);
	
	public boolean isCompatible(IGene<T> other);
	
	public IGene<T> cross(IGene<T> other, int minblock, int maxblock) throws IncompatibleParentsException;
	
	public T create(int posx, int posy, Environment e);
}
