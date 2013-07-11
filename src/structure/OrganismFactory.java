package structure;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import physics.PointMass;
import physics.Rod;

import bio.genetics.IGene;

import environment.Environment;

public class OrganismFactory {
	
	public static Organism fromGene(IGene<Organism> g, Environment e){
		double[] bounds = e.getBounds();
		int posx = (int) (Math.random()*(bounds[2]-bounds[0]) + bounds[0]);
		int posy = (int) (Math.random()*(bounds[3]-bounds[1]) + bounds[1]);
		return g.create(posx, posy, e);
	}
	
	
	public static Organism testDummy(Environment e){
		double[] bounds = e.getBounds();
		Organism o = new Organism(
				Math.random()*(bounds[2]-bounds[0]) + bounds[0],
				Math.random()*(bounds[3]-bounds[1]) + bounds[1],
				e);
		
		PointMass pm0 = new PointMass(1); 
		PointMass pm1 = new PointMass(1); 
		PointMass pm2 = new PointMass(1); 
		PointMass pm3 = new PointMass(0.5);
		List<PointMass> pmlist = new LinkedList<PointMass>();
		pmlist.add(pm0);
		pmlist.add(pm1);
		pmlist.add(pm2);
		pmlist.add(pm3);
		
		Rod a = new Rod(30, pm0, pm1);
		Rod b = new Rod(30, pm2, pm1);
		Rod c = new Rod(30, pm0, pm2);
		Rod d = new Rod(50, pm0, pm3);
		List<Rod> rodlist = new LinkedList<Rod>();
		rodlist.add(a);
		rodlist.add(b);
		rodlist.add(c);
		rodlist.add(d);
		
		o.addAllPointMasses(pmlist);
		o.addAllRods(rodlist);
		o.initStructure();
		
		return o;
	}
}
