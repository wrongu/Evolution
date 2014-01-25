package bio.genetics;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;

import environment.Environment;

// TODO metadata (for example, let the gene specify its own mutation rate)
public abstract class Gene<T> {
	
	/** The maximum rate of change for mutation rates (+/-) */
	private static final double META_MUTATION = 0.01;
	
	/** A Gene subclass may specify multiple named mutation rates which change according to
	 * the meta mutation-rate */
	private HashMap<String, Double> mutation_rates;
	
	protected Gene(){
		this.mutation_rates = new HashMap<String, Double>();
	}
	
	protected void initMutable(String k, double v){
		this.mutation_rates.put(k,  v);
	}
	
	protected double mutationRate(String k){
		if(this.mutation_rates.containsKey(k))
			return this.mutation_rates.get(k);
		else return 0.0;
	}
	
	protected void metaMutate(Random r){
		for(Entry<String, Double> pair : mutation_rates.entrySet()){
			double val = pair.getValue();
			val += (r.nextDouble() * 2.0 - 1.0) * META_MUTATION;
			pair.setValue(val);
		}
	}
	
	/** make a copy and alter its parameters randomly
	 * subclasses should call super.metaMutate() */
	public abstract Gene<T> mutate();
	
	public abstract T create(double posx, double posy, Environment e);
	
	public abstract void serialize(OutputStream s) throws IOException;
	
	public abstract void deserialize(InputStream in) throws IOException;
}
