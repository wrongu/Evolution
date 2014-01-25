package bio.genetics;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import environment.Environment;

// TODO metadata (for example, let the gene specify its own mutation rate)
public abstract class Gene<T> {
	public abstract Gene<T> mutate(double rate);
	
	public abstract T create(double posx, double posy, Environment e);
	
	public abstract void serialize(OutputStream s) throws IOException;
	
	public abstract void deserialize(InputStream in) throws IOException;
}
