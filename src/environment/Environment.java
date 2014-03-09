package environment;

import graphics.IDrawable;
import graphics.opengl.IDrawableGL;

import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import bio.genetics.DigraphGene;
import neuralevo.Grid;
import neuralevo.NOrganism;
import neuralevo.Square;

public class Environment implements IDrawable, IDrawableGL {
	
	public static final double MOUSE_CONSTANT = 0.2;
	public static final int LISTEN_SQUARES = (int)(NOrganism.LISTEN_RANGE/Square.SIZE) + 1;
	
	private Grid grid;
	private LinkedList<NOrganism> offspring;
	private int currentTick;
	private int ticksPerRemoveEmpties = 60*5;
	
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
		grid = new Grid();
		offspring = new LinkedList<NOrganism>();
		width = w;
		height = h;
		seedRand = new Random(seed);
		
		// NOrganism tests.
		for(int i = 0; i < 200; i++)
			grid.add(new NOrganism((Math.random() - 0.5)*width, (Math.random()-0.5)*height));
			//grid.add(new NOrganism());
		
		// DEBUGGING
		//nOrganisms.get(0).printBrain();
	}
	
	public Random getRandom(){
		return seedRand;
	}
	
	public void update(double dt){
		mousex = mouse_in[0];
		mousey = mouse_in[1];
		
		// Add in new organisms.
		for(NOrganism o : offspring) {
			grid.add(o);
		}
		offspring.clear();
		
		dt /= 100.0;
//		for(Square s : grid){
//			for(NOrganism o : s) {
//				if(mouse_buttons[0] != 0) {
//					double dist = Math.sqrt((mousex - o.getX())*(mousex - o.getX()) + (mousey - o.getY())*(mousey - o.getY()));
//					o.addForce(MOUSE_CONSTANT*(mousex - o.getX()) / dist, MOUSE_CONSTANT*(mousey - o.getY())/ dist);
//					//System.out.println("Mouse down on: x = " + mousex + ", y = " + mousey + ".");
//				}
//			}
//		}
		
		// Tick brains.
		for(Square s : grid) {
			for(Iterator<NOrganism> i = s.iterator(); i.hasNext(); ) {
				NOrganism o = i.next();
				o.tick();
//				o.printStats();		// DEBUGGING
				
				// Bury the dead.
				if(!o.isAlive()) {
					i.remove();
				}
			}
		}
		
		// Perform actions
		for(Square s : grid) {
			int i0 = s.getX();
			int j0 = s.getY();
			for(Iterator<NOrganism> iter = s.iterator(); iter.hasNext(); ) {
				NOrganism o = iter.next();
				o.updateActions();
				
				// Bury the dead.
				if(!o.isAlive()) {
					iter.remove();
				}
			}
			
			// Attack.
			for(Iterator<NOrganism> iter = s.iterator(); iter.hasNext(); ) {
				NOrganism o = iter.next();
				Square targetSquare;
				for(int i = -1; i <= 1; i++) {
					for(int j = -1; j <= 1; j++) {
						targetSquare = grid.get(i + i0, j + j0);
						if(targetSquare == null)
							continue;
						for(NOrganism p : targetSquare) {
							if(o != p)
								o.attack(p);
						}
					}
				}
			}
			
			// Bury the dead.
			for(Iterator<NOrganism> deathTrain = s.iterator(); deathTrain.hasNext(); ) {
				NOrganism o = deathTrain.next();
				if(!o.isAlive()) {
					deathTrain.remove();
				}
			}
			
			// Mate
			NOrganism child;
			for(Iterator<NOrganism> iter = s.iterator(); iter.hasNext(); ) {
				NOrganism o = iter.next();
				Square targetSquare;
				for(int i = 0; i <= 1; i++) {
					for(int j = 0; j <= 1; j++) {
						targetSquare = grid.get(i + i0, j + j0);
						if(targetSquare == null)
							continue;
						for(NOrganism p : targetSquare) {
							if(o == p)
								continue;
							child = o.mateWith(p);
							if(child == null) 
								continue;
							offspring.add(child);
						}
					}
				}
				
				// Bury the dead.
				if(!o.isAlive()) {
					iter.remove();
				}
			}
			
		}
		
		// Update senses.
		for(Square s : grid) {
			
			int i0 = s.getX();
			int j0 = s.getY();
			
			for(NOrganism o : s) {
				
				// Update listen sense.
				Square targetSquare;
				for(int i = -LISTEN_SQUARES; i <= LISTEN_SQUARES; i++) {
					for(int j = -LISTEN_SQUARES; j <= LISTEN_SQUARES; j++) {
						targetSquare = grid.get(i + i0,j + j0);
						if(targetSquare == null)
							continue;
						
						for(NOrganism p : targetSquare) {
							if(o != p)
								o.listenTo(p);
						}
					}
				}
				
				// Update touch sense.
				for(int i = -1; i <= 1; i++) {
					for(int j = -1; j <= 1; j++) {
						targetSquare = grid.get(i + i0, j + j0);
						if(targetSquare == null)
							continue;
						
						for(NOrganism p : targetSquare) {
							if(o != p)
								o.touching(p);
						}
					}
				}
			}
			
			for(NOrganism o : s) {
				o.updateSenses();
			}
		}
		
		LinkedList<NOrganism> toAdd = new LinkedList<NOrganism>();
		// Update the organism physics.
		for(Square s : grid) {
			for(Iterator<NOrganism> i = s.iterator(); i.hasNext(); ) {
				NOrganism o = i.next();
				
				// Move
				o.move(dt > 1.0 ? 1.0 : dt);
				
				// Update grid
				int coords[] = new int[2];
				coords[0] = (int)(o.getX()/Square.SIZE);
				coords[1] = (int)(o.getY()/Square.SIZE);
				if(coords[0] != s.getX() || coords[1] != s.getY()) {
					i.remove();
					toAdd.add(o);
				}
			}
		}
		for(NOrganism o : toAdd) {
			grid.add(o);
		}
		toAdd.clear();
		
		currentTick = (currentTick+1)%ticksPerRemoveEmpties;
		if(currentTick == 0)
			grid.removeEmpties();
		
		// DEBUGGING
//		grid.print();
	}

	public void draw(Graphics2D g, int sx, int sy, double scx, double scy) {
//		for(NOrganism o : nOrganisms)
//			o.draw(g, sx, sy, scx, scy);
	}
	
	public void glDraw() {
		// TODO - draw some sort of background?
		for(Square s : grid) {
			for(NOrganism o : s)
				o.glDraw();
		}
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
