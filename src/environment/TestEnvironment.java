package environment;

import structure.Organism;
import structure.OrganismFactory;
import bio.genetics.DigraphGene;

public class TestEnvironment extends Environment {
	
	public final double GRAVITY = 0.1;
	public final double MOUSE_CONSTANT = 0.1;
	public final static double SIZE = 1000.;
	
	private int[] mouse_in;
	private int[] mouse_buttons;
	private int mousex, mousey;
	private boolean spaceIsPressed;
	
	// TESTING
	private DigraphGene testgene;
	
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
		organisms.add(OrganismFactory.fromGene(testgene, this));
	}
	
	public void update(double dt){
		super.update(dt);
		
		for(Organism o : organisms){			
			 if(mouse_buttons[0] != 0) {
			 	double dist = Math.sqrt((mousex - o.getX())*(mousex - o.getX()) + (mousey - o.getY())*(mousey - o.getY()));
//			 	o.drift((mousex - o.getX()) / dist, (mousey - o.getY())/ dist);
			 	o.getPoints().get(0).addForce(MOUSE_CONSTANT*(mousex - o.getX()) / dist, MOUSE_CONSTANT*(mousey - o.getY())/ dist);
			 	//System.out.println("Mouse down on: x = " + mousex + ", y = " + mousey + ".");
			 }
			 
			 try{
			 if(spaceIsPressed)
				 o.getFirstMuscle().setStrength(-1);
			 else
				 o.getFirstMuscle().setStrength(0.2);
			 } catch(Exception e){}
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
	public void mutateTestGene(){
		testgene.mutate(seedRand);
		Organism cur = this.organisms.get(0);
		Organism evolved = testgene.create(cur.getX(), cur.getY(), this);
		this.organisms.clear();
		this.organisms.add(evolved);
		System.out.println("=======================");
		System.out.println(testgene);
	}

}
