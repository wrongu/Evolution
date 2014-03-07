package environment;

import graphics.IDrawable;
import graphics.opengl.IDrawableGL;

import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import bio.genetics.DigraphGene;

import neuralevo.NOrganism;

public class Environment implements IDrawable, IDrawableGL {
	
	public static final double GRAVITY = 0.1;
	public static final double MOUSE_CONSTANT = 0.2;
	public static final double FRICTION = 0.1;
	public static final double VISCOSITY = 0.004;

//	public List<Organism> organisms;
	public List<NOrganism> nOrganisms;
	public List<NOrganism> deaduns;
	private int width, height;
	
	private Random seedRand;
	
	private int[] mouse_in;
	private int[] mouse_buttons;
	private int mousex, mousey;
	private boolean spaceIsPressed;
	
	public Environment(int w, int h){
		this(w, h, 12345L);
	}
	
	public Environment(int w, int h, long seed){
//		organisms = new LinkedList<Organism>();
		nOrganisms = new LinkedList<NOrganism>();
		deaduns = new LinkedList<NOrganism>();
		width = w;
		height = h;
		seedRand = new Random(seed);
		
		// NOrganism tests.
		for(int i = 0; i < 100; i++)
			nOrganisms.add(new NOrganism());
		
		// DEBUGGING
		//nOrganisms.get(0).printBrain();
	}
	
	public Random getRandom(){
		return seedRand;
	}
	
	public void update(double dt){
		mousex = mouse_in[0];
		mousey = mouse_in[1];
		
		dt /= 100.0;
		for(NOrganism o : nOrganisms){
			
			 if(mouse_buttons[0] != 0) {
			 	double dist = Math.sqrt((mousex - o.getX())*(mousex - o.getX()) + (mousey - o.getY())*(mousey - o.getY()));
			 	o.addForce(MOUSE_CONSTANT*(mousex - o.getX()) / dist, MOUSE_CONSTANT*(mousey - o.getY())/ dist);
			 	//System.out.println("Mouse down on: x = " + mousex + ", y = " + mousey + ".");
			 }
		}
		
		// Tick brains.
		for(NOrganism o : nOrganisms) {
			o.tick();
//			o.printStats();								// DEBUGGING
			if(!o.isAlive()) {
				deaduns.add(o);
			}
		}
		
		// Bury the dead.
		for(NOrganism o : deaduns) {
			nOrganisms.remove(o);
		}
		deaduns.clear();
		
		// Perform actions
		for(NOrganism o : nOrganisms) {
			o.reflexiveActions();
			if(!o.isAlive()) {
				deaduns.add(o);
			}
		}
		
		// Bury the dead.
		for(NOrganism o : deaduns) {
			nOrganisms.remove(o);
		}
		deaduns.clear();
		
		// Update senses.
		for(NOrganism o : nOrganisms) {
			o.internalSenses();
		}
		
		// Update the organism physics.
		for(NOrganism o : nOrganisms) {
			o.move(dt > 1.0 ? 1.0 : dt);
		}
		
		
		
	}

	public void draw(Graphics2D g, int sx, int sy, double scx, double scy) {
//		for(NOrganism o : nOrganisms)
//			o.draw(g, sx, sy, scx, scy);
	}
	
	public void glDraw() {
		// TODO - draw some sort of background?
		for(NOrganism o : nOrganisms)
			o.glDraw();
	}
	
	/**
	 * get boundaries of this environment
	 * @return double array [xmin, ymin, xmax, ymax] of environment's bounding area
	 */
	public double[] getBounds(){
		return new double[] {-width/2, -height/2, width/2, height/2};
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
//		Organism cur = this.organisms.get(0);
//		Organism evolved = testgene.create(cur.getX(), cur.getY(), this);
//		this.organisms.clear();
//		this.organisms.add(evolved);
//		System.out.println("=======================");
//		System.out.println(testgene);
//	}

}
