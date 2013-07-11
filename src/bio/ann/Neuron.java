package bio.ann;

import java.util.ArrayList;

public class Neuron {
	
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
