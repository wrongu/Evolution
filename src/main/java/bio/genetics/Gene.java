package bio.genetics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import applet.Config;

import environment.Environment;

public abstract class Gene<T> {
	
	/** The maximum rate of change for mutation rates (+/-) */
	private static final double META_MUTATION_RATE = Config.instance.getDouble("METAMUTATION");
	
	/** A Gene subclass may specify multiple named mutation rates which change according to
	 * the meta mutation-rate */
	private HashMap<String, Double> mutation_rates;
	
	protected Gene(String ... keys){
		this.mutation_rates = new HashMap<String, Double>();
		for(String k: keys){
			this.mutation_rates.put(k, 0.0);
		}
	}
	
	public final Gene<T> clone(){
		Gene<T> copy = this._clone();
		for(Map.Entry<String, Double> e : copy.mutation_rates.entrySet()){
			copy.mutation_rates.put(e.getKey(), e.getValue());
		}
		return this;
	}
	
	protected abstract Gene<T> _clone();
	
	protected final double mutationRate(String k){
		if(this.mutation_rates.containsKey(k))
			return this.mutation_rates.get(k);
		else return 0.0;
	}
	
	private final void metaMutate(Random r){
		for(Entry<String, Double> pair : mutation_rates.entrySet()){
			double val = pair.getValue();
			double unit_random = r.nextDouble() - r.nextDouble();
			val *= Math.exp(unit_random * META_MUTATION_RATE);
			val = Math.min(Math.max(0.0, val), 1.0); // clamp between 0 and 1
			pair.setValue(val);
		}
	}
	
	/**
	 * Mutates and returns a *copy* of this gene
	 */
	public Gene<T> mutate(Random r){
		Gene<T> child = this.clone();
		child.metaMutate(r);
		child._mutate(r);
		return child;
	}
	
	/** make a copy and alter its parameters randomly
	 * subclasses should call super.metaMutate() */
	// TODO the abstract/final trick so that subclasses aren't responsible for super.metaMutate()
	protected abstract void _mutate(Random r);
	
	public abstract T create(double posx, double posy, Environment e);
	
	public final void serialize(DataOutputStream dest) throws IOException {
		// write mutation rates
		dest.writeInt(this.mutation_rates.size());
		for(Entry<String, Double> pair : this.mutation_rates.entrySet()){
			String key = pair.getKey();
			dest.writeInt(key.length());
			for(int i=0; i<key.length(); i++)
				dest.writeChar(key.charAt(i));
			dest.writeDouble(pair.getValue());
		}
		this._serialize(dest);
	}
	
	protected abstract void _serialize(DataOutputStream s) throws IOException;
	
	public final void deserialize(DataInputStream src) throws IOException{
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
		this._deserialize(src);
	}
	protected abstract void _deserialize(DataInputStream s) throws IOException;
}
