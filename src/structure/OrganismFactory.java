package structure;

import java.util.LinkedList;
import java.util.List;

import physics.Joint;
import physics.PointMass;
import physics.Rod;
import utils.MathHelper;
import bio.genetics.CellDivideGene;
import bio.genetics.SexGene;
import environment.Environment;

public class OrganismFactory {
	
	public static final int TRIANGLE_WITH_TAIL = 0;
	public static final int TWO_RODS_WITH_JOINT = 1;
	public static final int JOINTLESS_SNAKE = 2;
	public static final int TRIANGLE_WITH_MUSCLE = 3;
	public static final int SNAKE_WITH_JOINTS = 4;
	public static final int SIMPLE_JELLYFISH = 5;
	public static final int GENE_TEST = 6;
	public static final int POINT_MASS = 7;
	public static final int DUMBELL = 8;
	
	public static Organism fromGene(SexGene<Organism> g, Environment e){
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
		List<Muscle> musclelist = new LinkedList<Muscle>();
		
		double cx = (bounds[2] + bounds[0]) / 2.0;
		double cy = (bounds[3] + bounds[1]) / 2.0;
		
		Organism o = new Organism(e);
		
		int n;

		switch(creature) {
		
		case TRIANGLE_WITH_TAIL:
			PointMass pm0 = new PointMass(1);
				pm0.initPosition(cx + Math.random()*30., cy+Math.random()*30.);
			PointMass pm1 = new PointMass(1); 
				pm1.initPosition(cx + Math.random()*30., cy+Math.random()*30.);
			PointMass pm2 = new PointMass(1); 
				pm2.initPosition(cx + Math.random()*30., cy+Math.random()*30.);
			PointMass pm3 = new PointMass(0.5);
				pm3.initPosition(cx + Math.random()*30., cy+Math.random()*30.);
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
			for(int i = 0; i < 3; i++){
				PointMass chain = new PointMass(1);
					chain.initPosition(cx + Math.random()*30., cy+Math.random()*30.);
				pmlist.add(chain);
			}
			rodlist.add(new Rod(50, 50, pmlist.get(1), pmlist.get(0)));
			rodlist.add(new Rod(50, 50, pmlist.get(2), pmlist.get(0)));
			jointlist.add(new Joint(0.3*Math.PI, 0.9*Math.PI, pmlist.get(0), rodlist.get(0), rodlist.get(1) ));
			break;
			
		case JOINTLESS_SNAKE:
			n = 8;
			double width = n*30.0;
			for(int i = 0; i < n+1; i++){
				PointMass pm = new PointMass((double)1/(double)(i*i));
					pm.initPosition(cx - width/2.0 + i*30., cy);
				pmlist.add(pm);
			}
			for(int i = 0; i < n; i++){
				rodlist.add(new Rod(30, 30, pmlist.get(i), pmlist.get(i+1)));
			}
			break;
			
		case TRIANGLE_WITH_MUSCLE:
			for(int i = 0; i < 3; i++) {
				PointMass pm = new PointMass(1);
				pm.initPosition(cx + Math.random()*30., cy+Math.random()*30.);
				pmlist.add(pm);
			}
			rodlist.add(new Rod(50,50, pmlist.get(0), pmlist.get(1)));
			rodlist.add(new Rod(50,50, pmlist.get(0), pmlist.get(2)));
			rodlist.add(new Rod(30,70, pmlist.get(1), pmlist.get(2)));
			musclelist.add(new Muscle(rodlist.get(2), 2));
			break;
			
		case SNAKE_WITH_JOINTS:
			n = 6;
			double width2 = n * 30.;
			for(int i = 0; i < n+1; i++){
				PointMass chain = new PointMass((double)1/(double)(i*i));
				chain.initPosition(cx - width2/2.0 + i*30., cy);
				pmlist.add(chain);
			}
			for(int i = 0; i < n; i++)
				rodlist.add(new Rod(30, 30, pmlist.get(i), pmlist.get(i+1)));
			for(int i = 1; i < n; i++)
				jointlist.add(new Joint(0.7*Math.PI, 1.3*Math.PI, pmlist.get(i), rodlist.get(i-1), rodlist.get(i)));
			break;
			
		case SIMPLE_JELLYFISH:
			for(int i = 0; i < 3; i++){
				PointMass pm = new PointMass(1);
				pm.initPosition(cx + Math.random() * 50, cy + Math.random() * 50);
				pmlist.add(pm);
			}
			rodlist.add(new Rod(50, 50, pmlist.get(1), pmlist.get(0)));
			rodlist.add(new Rod(50, 50, pmlist.get(2), pmlist.get(0)));
			jointlist.add(new Joint(0.15*Math.PI, 0.9*Math.PI, pmlist.get(0), rodlist.get(0), rodlist.get(1) ));
			musclelist.add(new Muscle(jointlist.get(0),2));
			break;
			
		case GENE_TEST:
			StringBuilder builder = new StringBuilder();
			MathHelper.writeIntAsHex(builder, 16);
			
			builder.append("#S");
			MathHelper.writeIntAsHex(builder, 0);
			MathHelper.writeIntAsHex(builder, 3);
			MathHelper.writeIntAsHex(builder, 1);
			MathHelper.writeFloatAsHex(builder, 20f);
			MathHelper.writeFloatAsHex(builder, 40f);
			builder.append('.');
			builder.append('S');
			MathHelper.writeIntAsHex(builder, 1);
			MathHelper.writeIntAsHex(builder, 3);
			MathHelper.writeIntAsHex(builder, 2);
			MathHelper.writeFloatAsHex(builder, 40f);
			MathHelper.writeFloatAsHex(builder, 60f);
			
			String gene = builder.toString();
			System.out.println(gene);
			return new CellDivideGene(gene).create(0, 0, e);
			
		case POINT_MASS:
			pmlist.add(new PointMass(1));
			break;
			
		case DUMBELL:
			pmlist.add(new PointMass(1));
			pmlist.add(new PointMass(1));
			rodlist.add(new Rod(100,100,pmlist.get(0), pmlist.get(1)));
			break;
		}
		
		o.addAllPointMasses(pmlist);
		o.addAllRods(rodlist);
		o.addAllJoints(jointlist);
		o.addAllMuscles(musclelist);
		o.initStructure();
		
		return o;
	}
}
