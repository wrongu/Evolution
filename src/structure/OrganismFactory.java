package structure;

import java.util.HashMap;

import bio.Gene;

import environment.Environment;

public class OrganismFactory {
	
	public static Organism fromGene(Gene g, Environment e){
		HashMap<Integer, Structure> id_to_struct_map;
		Brain brain;
		double[] bounds = e.getBounds();
		Organism o = new Organism(
				Math.random()*(bounds[2]-bounds[0]) + bounds[0],
				Math.random()*(bounds[3]-bounds[1]) + bounds[1],
				e);
		// TODO - build it from gene
		return o;
	}
	
	public static Gene toGene(Organism o){
		Gene g = new Gene();
		// TODO - write to gene in such a way that o is the same as fromGene(toGene(o), e)
		return g;
	}
}
