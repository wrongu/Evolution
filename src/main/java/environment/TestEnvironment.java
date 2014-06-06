package environment;

import utils.grid.Chunk;
import bio.genetics.Gene;
import bio.organisms.AbstractOrganism;
import bio.organisms.Entity;
import bio.organisms.SimpleCircleOrganism;

public class TestEnvironment extends Environment {

	public final double GRAVITY = 0.1;
	public final double MOUSE_CONSTANT = 0.5;
	public final static double SIZE = 1000.;
	public final static double FOOD_RADIUS = 2*SimpleCircleOrganism.DEFAULT_RANGE;

	private int[] mouse_in;
	private int[] mouse_buttons;
	private boolean keep_alive;
	private boolean spaceIsPressed;

	public TestEnvironment(long seed, boolean keep_alive){
		super(SIZE, SIZE, Topology.TORUS, seed);
		for(int i = 0; i < 200; i++)
			grid.add(new SimpleCircleOrganism(this, 1.0, (getRandom().nextDouble() - 0.5)*SIZE, (getRandom().nextDouble() - 0.5)*SIZE));
		this.keep_alive = keep_alive;
	}

	public void update(double dt){
		super.update(dt);

		for(AbstractOrganism ao : grid) {
			if(keep_alive) ((AbstractOrganism)ao).feed(10.0);
			if(mouse_buttons[0] != 0) {
				double dist = Math.hypot((mouse_in[0] - ao.getX()), (mouse_in[1] - ao.getY()));
				if(ao instanceof SimpleCircleOrganism)
					((SimpleCircleOrganism) ao).addExternalForce(MOUSE_CONSTANT*(mouse_in[0] - ao.getX()) / dist, MOUSE_CONSTANT*(mouse_in[1] - ao.getY())/ dist);
			}

			//				try{
			//					if(spaceIsPressed)
			//						ao.getFirstMuscle().act(0.2);
			//					else
			//						ao.getFirstMuscle().act(0.0);
			//				} catch(Exception e){}
		}

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
