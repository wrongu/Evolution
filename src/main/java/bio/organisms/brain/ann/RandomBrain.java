package bio.organisms.brain.ann;

import java.util.Random;

import environment.Environment;
import bio.genetics.Gene;
import bio.organisms.AbstractOrganism;
import bio.organisms.brain.IBrain;

public class RandomBrain implements IBrain {
	
	private Random rand;
	
	public RandomBrain(Random r){
		rand = r;
	}
	
	public IBrain beget(Environment e, AbstractOrganism parent) {
		return new RandomBrain(e.getRandom());
	}

	public void tick() {}

	public void setInput(int id, double val) {}

	public double getOutput(int id) {
		return rand.nextDouble();
	}

	public Gene<? extends IBrain> getGene() {
		return null;
	}

}
