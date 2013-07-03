package bio;

import java.util.HashMap;
import java.util.Map;

public class NeuronConnection {
	
	public static Map<Neuron, Neuron> NEURON_MAP;
	public static final int TYPE_IN  = 0;
	public static final int TYPE_OUT = 1;
	
	private float weight;
	private Neuron from, to;
	
	public NeuronConnection(float w, Neuron f, Neuron t){
		weight = w;
		from = f;
		to = t;
		
		from.connect(this);
		to.connect(this);

		NEURON_MAP.put(from, to);
	}
	
	public void activate(){
		to.addActivation(from.getActivation() * weight);
	}
	
	public Neuron getFrom(){
		return from;
	}
	
	public Neuron getTo(){
		return to;
	}
	
	static{
		NEURON_MAP = new HashMap<Neuron, Neuron>();
	}
}
