package bio.organisms.brain.ann;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ANN {
	
	private List<Neuron> neurons;
	private List<NeuronConnection> connections;
	
	public ANN(){
		neurons = new ArrayList<Neuron>();
		connections = new ArrayList<NeuronConnection>();
	}
	
	public void addNeuron(Neuron n){
		addNeuron(n, Neuron.TYPE_HIDDEN);
	}
	
	public void addNeuron(Neuron n, int type){
		n.setType(type);
		neurons.add(n);
	}
	
	public void addConnection(Neuron a, Neuron b, float weight){
		addConnection(new NeuronConnection(weight, a, b));
	}
	
	public void addConnection(NeuronConnection conn){
		connections.add(conn);
		if(!neurons.contains(conn.getFrom())) neurons.add(conn.getFrom());
		if(!neurons.contains(conn.getTo()))   neurons.add(conn.getTo());
	}
	
	public void nextState(){
		for(NeuronConnection nc : connections)
			nc.activate();
		for(Neuron n : neurons)
			n.flush();
	}
	
	public static class Neuron {
		
		private static final float DEFAULT_ACTION_POTENTIAL = 10F;
		private static final float DEPOLARIZATION = -3F;
		private static final float DECAY = 0.95F;

		public static final int TYPE_INPUT  = 0;
		public static final int TYPE_HIDDEN = 1;	
		public static final int TYPE_OUTPUT = 2;
		
		private float threshold;
		private float action_potential;
		private float activation;
		private float next_activation;
		private int type;
		private ArrayList<NeuronConnection> conn_in, conn_out;
		
		public Neuron(float thresh, int t, float ap){
			threshold = thresh;
			activation = 0f;
			action_potential = ap;
			type = t;
			conn_in  = new ArrayList<NeuronConnection>();
			conn_out = new ArrayList<NeuronConnection>();
		}
		
		public Neuron(float thresh){
			this(thresh, TYPE_HIDDEN, DEFAULT_ACTION_POTENTIAL);
		}
		
		public Neuron(float thresh, float ap){
			this(thresh, TYPE_HIDDEN, ap);
		}
		
		public void connect(NeuronConnection pair){
			if(pair.getFrom() == this){
				conn_out.add(pair);
			} else if(pair.getTo() == this){
				conn_in.add(pair);
			}
		}
		
		public void addActivation(float amount){
			next_activation += amount;
		}
		
		public void flush(){
			if(next_activation > threshold){
				activation = action_potential;
				next_activation = DEPOLARIZATION;
			} else{
				activation = next_activation * DECAY;
				next_activation = 0F;
			}
		}
		
		public float getActivation(){
			return activation;
		}
		
		public int getType(){
			return type;
		}
		
		public void setType(int t){
			type = t;
		}
	}
	
	public static class NeuronConnection {
		
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
}