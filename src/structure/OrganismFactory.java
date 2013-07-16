package structure;

import java.util.LinkedList;
import java.util.List;

import physics.Joint;
import physics.PointMass;
import physics.Rod;
import bio.genetics.IGene;
import environment.Environment;

public class OrganismFactory {
	
	public static final int TRIANGLE_WITH_TAIL = 0;
	public static final int TWO_RODS_WITH_JOINT = 1;
	public static final int JOINTLESS_SNAKE = 2;
	public static final int TRIANGLE_WITH_MUSCLE = 3;
	public static final int SNAKE_WITH_JOINTS = 4;
	
	public static Organism fromGene(IGene<Organism> g, Environment e){
		double[] bounds = e.getBounds();
		int posx = (int) (Math.random()*(bounds[2]-bounds[0]) + bounds[0]);
		int posy = (int) (Math.random()*(bounds[3]-bounds[1]) + bounds[1]);
		return g.create(posx, posy, e);
	}
	
	
	public static Organism testDummy(int creature, Environment e){
		double[] bounds = e.getBounds();
		List<PointMass> pmlist = new LinkedList<PointMass>();
		List<Rod> rodlist = new LinkedList<Rod>();
		List<Joint> jointlist = new LinkedList<Joint>();
		
		Organism o = new Organism(
				Math.random()*(bounds[2]-bounds[0]) + bounds[0],
				Math.random()*(bounds[3]-bounds[1]) + bounds[1],
				e);
		
		int n;

		switch(creature) {
		
		case TRIANGLE_WITH_TAIL:
			PointMass pm0 = new PointMass(1); 
			PointMass pm1 = new PointMass(1); 
			PointMass pm2 = new PointMass(1); 
			PointMass pm3 = new PointMass(0.5);
			pmlist.add(pm0);
			pmlist.add(pm1);
			pmlist.add(pm2);
			pmlist.add(pm3);
			
			Rod a = new Rod(30, 30, pm0, pm1);
			Rod b = new Rod(30, 30, pm2, pm1);
			Rod c = new Rod(30, 30, pm0, pm2);
			Rod d = new Rod(50, 50, pm0, pm3);
			rodlist.add(a);
			rodlist.add(b);
			rodlist.add(c);
			rodlist.add(d);
			break;
			
		case TWO_RODS_WITH_JOINT:
			for(int i = 0; i < 3; i++)
				pmlist.add(new PointMass(1));
			rodlist.add(new Rod(50, 50, pmlist.get(1), pmlist.get(0)));
			rodlist.add(new Rod(50, 50, pmlist.get(2), pmlist.get(0)));
			jointlist.add(new Joint(0.3*Math.PI, 0.9*Math.PI, pmlist.get(0), rodlist.get(0), rodlist.get(1) ));
			break;
			
		case JOINTLESS_SNAKE:
			n = 8;
			for(int i = 0; i < n+1; i++)
				pmlist.add(new PointMass((double)1/(double)(i*i)));
			for(int i = 0; i < n; i++)
				rodlist.add(new Rod(30, 30, pmlist.get(i), pmlist.get(i+1)));
			break;
			
		case TRIANGLE_WITH_MUSCLE:
			for(int i = 0; i < 3; i++) {pmlist.add(new PointMass(1));}
			rodlist.add(new Rod(50,50, pmlist.get(0), pmlist.get(1)));
			rodlist.add(new Rod(50,50, pmlist.get(0), pmlist.get(2)));
			rodlist.add(new Rod(30,70, pmlist.get(1), pmlist.get(2)));
			break;
			
		case SNAKE_WITH_JOINTS:
			n = 6;
			for(int i = 0; i < n+1; i++)
				pmlist.add(new PointMass((double)1/(double)(i*i)));
			for(int i = 0; i < n; i++)
				rodlist.add(new Rod(30, 30, pmlist.get(i), pmlist.get(i+1)));
			for(int i = 1; i < n; i++)
				jointlist.add(new Joint(0.7*Math.PI, 1.3*Math.PI, pmlist.get(i), rodlist.get(i-1), rodlist.get(i)));
			break;
			
		}
		
		o.addAllPointMasses(pmlist);
		o.addAllRods(rodlist);
		o.addAllJoints(jointlist);
		o.initStructure();
		
		return o;
	}
}
