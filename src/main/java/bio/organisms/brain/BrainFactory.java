package bio.organisms.brain;

import java.util.Random;

import bio.organisms.AbstractOrganism;
import bio.organisms.brain.ann.DumbBrain;
import bio.organisms.brain.ann.DumberBrain;
import bio.organisms.brain.ann.DumbestBrain;

public class BrainFactory {
	
	public static IBrain newDumbBrain(int s, int o, AbstractOrganism host, Random r){
		return DumbBrain.newRandom(s, o, host, r);
	}
	
	public static IBrain newDumberBrain(int s, int o, AbstractOrganism host, Random r){
		return DumberBrain.newRandom(s, o, host, r);
	}
	
	public static IBrain newDumbestBrain(int s, int o, AbstractOrganism host, Random r){
		return DumbestBrain.newRandom(s, o, host, r);
	}
	
	public static IBrain newBrain(String type, int s, int o, AbstractOrganism host, Random r){
		if(type.equals("DumbBrain")) return newDumbBrain(s, o, host, r);
		else if(type.equals("DumberBrain")) return newDumberBrain(s, o, host, r);
		else if(type.equals("DumbestBrain")) return newDumbestBrain(s, o, host, r);
		else return null;
	}
}
