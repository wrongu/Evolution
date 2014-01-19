package bio.genetics;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import environment.Environment;

public interface IGene<T> {
	public IGene<T> mutate(double rate);
	
	public T create(int posx, int posy, Environment e);
	
	public void serialize(OutputStreamWriter dest);
	
	public void deserialize(InputStreamReader reader);
}
