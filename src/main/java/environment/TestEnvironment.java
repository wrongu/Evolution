package environment;

import utils.grid.Chunk;
import bio.genetics.DigraphGene;
import bio.genetics.Gene;
import bio.organisms.AbstractOrganism;
import bio.organisms.OrganismFactory;
import bio.organisms.PointRodOrganism;

public class TestEnvironment extends Environment {

	public final double GRAVITY = 0.1;
	public final double MOUSE_CONSTANT = 0.1;
	public final static double SIZE = 1000.;

	private int[] mouse_in;
	private int[] mouse_buttons;
	private int mousex, mousey;
	private boolean spaceIsPressed;

	// TESTING
	private Gene<PointRodOrganism> testgene;

	public TestEnvironment(long seed){
		super(SIZE, SIZE, Topology.TORUS, seed);
		// DEBUGGING
		//		organisms.add(OrganismFactory.testDummy(OrganismFactory.SIMPLE_JELLYFISH,this));
		//		organisms.add(OrganismFactory.testDummy(OrganismFactory.GENE_TEST, this));
		//		organisms.add(OrganismFactory.testDummy(OrganismFactory.DUMBELL, this));
		//		for(int i = 0; i < 20; i++)
		//			organisms.add(OrganismFactory.testDummy(OrganismFactory.POINT_MASS, this));
		testgene = new DigraphGene();
		for(int i=0; i<100; i++) testgene.mutate(seedRand);
		grid.add(OrganismFactory.fromGene(testgene, this));
	}

	public void update(double dt){
		super.update(dt);

		for(Chunk c : grid) {
			for(AbstractOrganism ao : c){
				PointRodOrganism o = (PointRodOrganism) ao;
				if(mouse_buttons[0] != 0) {
					double dist = Math.sqrt((mousex - o.getX())*(mousex - o.getX()) + (mousey - o.getY())*(mousey - o.getY()));
					//			 	o.drift((mousex - o.getX()) / dist, (mousey - o.getY())/ dist);
					o.getPoints().get(0).addForce(MOUSE_CONSTANT*(mousex - o.getX()) / dist, MOUSE_CONSTANT*(mousey - o.getY())/ dist);
					//System.out.println("Mouse down on: x = " + mousex + ", y = " + mousey + ".");
				}

				try{
					if(spaceIsPressed)
						o.getFirstMuscle().act(0.2);
					else
						o.getFirstMuscle().act(0.0);
				} catch(Exception e){}
			}
		}
	}

	public void mouse_move(int mx, int my){
		mouse_buttons[0] = 1;
		mousex = mx;
		mousey = my;
	}

	public void space_press(boolean isPressed) {
		spaceIsPressed = isPressed;
	}

	public void bindInput(int[] mouse_buttons, int[] mouse_move) {
		this.mouse_buttons = mouse_buttons;
		this.mouse_in = mouse_move;
	}

	// TESTING ONLY
//	public void mutateTestGene(){
//		testgene.mutate(seedRand);
//		PointRodOrganism cur = (PointRodOrganism) this.organisms.get(0);
//		PointRodOrganism evolved = testgene.create(cur.getX(), cur.getY(), this);
//		this.grid.clear();
//		this.grid.add(evolved);
//		System.out.println("=======================");
//		System.out.println(testgene);
//	}

}
