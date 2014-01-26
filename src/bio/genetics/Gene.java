package bio.genetics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
	
	protected final void initMutable(String k){
		this.mutation_rates.put(k,  0.0);
	}
	
	protected final void initMutables(String ... keys){
		for(String k: keys){
			this.initMutable(k);
		}
	}
	
	protected final double mutationRate(String k){
		if(this.mutation_rates.containsKey(k))
			return this.mutation_rates.get(k);
		else return 0.0;
	}
	
	protected final void metaMutate(Random r){
		for(Entry<String, Double> pair : mutation_rates.entrySet()){
			double val = pair.getValue();
			val += (r.nextDouble() * 2.0 - 1.0) * META_MUTATION;
			val = Math.min(Math.max(0.0, val), 1.0); // clamp between 0 and 1
			pair.setValue(val);
		}
	}
	
	/** make a copy and alter its parameters randomly
	 * subclasses should call super.metaMutate() */
	public abstract Gene<T> mutate(Random r);
	
	public abstract T create(double posx, double posy, Environment e);
	
	public void serialize(OutputStream s) throws IOException {
		DataOutputStream dest = new DataOutputStream(s);
		// write mutation rates
		dest.writeInt(this.mutation_rates.size());
		for(Entry<String, Double> pair : this.mutation_rates.entrySet()){
			String key = pair.getKey();
			dest.writeInt(key.length());
			for(int i=0; i<key.length(); i++)
				dest.writeChar(key.charAt(i));
			dest.writeDouble(pair.getValue());
		}
	}
	
	public void deserialize(InputStream in) throws IOException{
		DataInputStream src = new DataInputStream(in);
		// read mutation rates
		int n_values = src.readInt();
		for(int i=0; i<n_values; i++){
			int k_len = src.readInt();
			StringBuilder key = new StringBuilder();
			for(int c=0; c<k_len; c++)
				key.append(src.readChar());
			double val = src.readDouble();
			this.mutation_rates.put(key.toString(), val);
		}
	}
}
