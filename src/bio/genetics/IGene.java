package bio.genetics;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import environment.Environment;

// TODO metadata (for example, let the gene specify its own mutation rate)
public interface IGene<T> {
	public IGene<T> mutate(double rate);
	
	public T create(double posx, double posy, Environment e);
	
	public void serialize(OutputStream s) throws IOException;
	
	public void deserialize(InputStream in) throws IOException;
}
