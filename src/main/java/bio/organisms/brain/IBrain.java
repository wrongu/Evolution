package bio.organisms.brain;

import bio.genetics.Gene;
import bio.genetics.IGeneCarrier;
import bio.organisms.AbstractOrganism;

public interface IBrain extends IGeneCarrier<IBrain, AbstractOrganism>{

	public void tick();
	
	public void setInput(int id, double val);
	
	public double getOutput(int id);
	
	public Gene<? extends IBrain> getGene();
}
