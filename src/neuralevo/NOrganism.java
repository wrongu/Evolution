package neuralevo;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glVertex2d;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glRotated;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_QUADS;

import javax.vecmath.Vector2d;

public class NOrganism {
	
	// Physical constants for organisms.
	public static final double RESISTANCE = 0.05;
	public static final double NORMAL_RESISTANCE_FACTOR = 0.5;
	public static final double DEFAULT_MASS = 1;
	public static final double RANGE = 10;
	public static final double LISTEN_RANGE = 500;
	public static final double MAX_TALK = 1;
	
	// Food and energy constants.
	public static final double FOOD_ON_DEATH = 10;
	public static final double CONSUMPTION_ROOT = 2; // 2 -> sqrt curve, 3 -> 3rd root curve, so on.
	public static final double CONSUMPTION_CURVATURE = 1; // larger means more abrupt consumption curve.
	public static final double ATTACK_COST = 1;
	public static final double TALK_COST = 0.0001;
	public static final double THRUST_COST = 0.0005;
	public static final double TURN_COST = 0.0005;
	
	// Action constants.
	public static final double THRUST_STRENGTH = 0.1;
	public static final double TURN_STRENGTH = 0.1;
	public static final double ATTACK_STRENGTH = 1;
	public static final double TALK_STRENGTH = 0.5;
	
	// Sensory constants.
	public static final double THRUST_SENSE = 1/THRUST_STRENGTH;
	public static final double TURN_SENSE = 1/TURN_STRENGTH;
	public static final double LISTEN_SENSE = 0.5;
	public static final double ENERGY_SENSE = 1;
	
	// Computational constants.
	public static final double EATING_STEP = 0.01; // Smaller = finer, larger = rougher.
	public static final double DRAW_SMOOTHNESS = 2; // Smaller = rougher, larger = smoother.
	
	// Physics fields.
	private Vector2d pos;
	private Vector2d acc;
	private double tacc;
	private double nacc;
	private double turn;
	private Vector2d vel;
	private Vector2d dir;
	private double spd;
	private double mass;
	
	// Organism fields.
	private NGene gene;
	private NBrain brain;
	private double energy;
	private double talk;
	private double listen;
	private double lastListen;
	
	/**
	 * Constructor for NOrganism class.
	 * 
	 * @param g Genes of new NOrganism
	 * @param e Initial energy
	 * @param p Initial position
	 * @param v Initial velocity
	 */
	public NOrganism(NGene g, double e, Vector2d p, Vector2d v){
		mass = DEFAULT_MASS;
		
		pos = new Vector2d(p);
		vel = new Vector2d(v);
		dir = new Vector2d();
		acc = new Vector2d();
		spd = vel.length();
		if(spd == 0) {
			double theta = 2*Math.PI*Math.random();
			dir.x = Math.cos(theta);
			dir.y = Math.sin(theta);
		} else {
			dir.x = vel.x/spd;
			dir.x = vel.y/spd;
		}
		
		gene = g;
		brain = new NBrain(g);
		
		energy = e;
	}
	
	public NOrganism(double x, double y) {
		this(new NGene(), 1, new Vector2d(x,y), new Vector2d());
	}
	
	public NOrganism() {
		this(new NGene(), 1, new Vector2d(), new Vector2d());
	}
	
	public void tick() {
		energy -= brain.tick();
	}
	
	public void move(double dt) {
		
		tacc -= RESISTANCE*spd/mass;
		dir.x = dir.x*Math.cos(turn*dt) - dir.y*Math.sin(turn*dt);
		dir.y = dir.x*Math.sin(turn*dt) + dir.y*Math.cos(turn*dt);
		spd += tacc*dt;
		
		vel.x = spd*dir.x;
		vel.y = spd*dir.y;
		
		vel.x += acc.x*dt;
		vel.y += acc.y*dt;
		pos.x += vel.x*dt;
		pos.y += vel.y*dt;
		acc.x = 0;
		acc.y = 0;
		tacc = 0;
		nacc = 0;
		turn = 0;
		
		spd = vel.length();
		if(spd > 0) {
			dir.x = vel.x/spd;
			dir.y = vel.y/spd;
		}
	}
	
	public void addForce(double x, double y) {
		acc.x += x/mass;
		acc.y += y/mass;
	}
	
	public boolean isAlive() {
		return energy > 0;
	}
	
	public void eat(double e) {
		if(e <= 0)
			return;
		
		while(e > EATING_STEP) {
			energy += e/Math.pow(CONSUMPTION_CURVATURE*energy + 1,CONSUMPTION_ROOT - 1);
			e -= EATING_STEP;
		}
		energy += e/Math.pow(CONSUMPTION_CURVATURE*energy + 1,CONSUMPTION_ROOT - 1);
	}
	
	public void glDraw() {

		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		
		glBegin(GL_QUADS);
		{
			glVertex2d(pos.x + 2*dir.x, pos.y + 2*dir.y);
			glVertex2d(pos.x + 2*dir.y, pos.y - 2*dir.x);
			glVertex2d(pos.x - 2*vel.x - 2*dir.x, pos.y - 2*vel.y - 2*dir.y);
			glVertex2d(pos.x - 2*dir.y, pos.y + 2*dir.x);
		}
		glEnd();
		int n = (int)(RANGE*DRAW_SMOOTHNESS);
		double t = 2*Math.PI/(double)n;
		glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
		glBegin(GL_LINES);
		{
			for(int i = 0; i < n; i++) {
				glVertex2d(pos.x + RANGE*Math.cos(i*t), pos.y + RANGE*Math.sin(i*t) );
				glVertex2d(pos.x + RANGE*Math.cos((i+1)*t), pos.y + RANGE*Math.sin((i+1)*t));
			}
		}
		glEnd();
	}
	
	public double getX() { return pos.x; }
	public double getY() { return pos.y; } 
	
	public void reflexiveActions() {
		// Thrust action
		double thrustOut = brain.output(NBrain.THRUST_OUT);
		energy -= THRUST_COST*Math.abs(thrustOut);
		tacc += THRUST_STRENGTH*thrustOut;
//		addForce(THRUST_STRENGTH*thrustOut*dir.x, THRUST_STRENGTH*thrustOut*dir.y);

		// Turn action
		double turnOut = brain.output(NBrain.TURN_OUT);
		energy -= TURN_COST*Math.abs(turnOut);
		turn = TURN_STRENGTH*turnOut;
		
		// Talk action
		double talkOut = brain.output(NBrain.TALK);
		energy -= TALK_COST*Math.abs(talkOut);
		talk = TALK_STRENGTH*talkOut;
	}
	
	public void internalSenses() {
		// Energy sense
		brain.input(NBrain.ENERGY, ENERGY_SENSE*energy);
		
		// Thrust sense
		brain.input(NBrain.THRUST_IN, THRUST_SENSE*acc.dot(dir));
		
		// Turn sense
		brain.input(NBrain.TURN_IN, TURN_SENSE*(dir.x*acc.y - dir.y*acc.x) );
		
		// Update listen
		brain.input(NBrain.LISTEN, listen - lastListen);
		lastListen = listen;
		listen = 0;
	}
	
	public void listenTo(NOrganism o) {
		double dx = pos.x - o.pos.x;
		double dy = pos.y - o.pos.y;
		double dist = Math.hypot(dx, dy);
		
		if(dist > LISTEN_RANGE)
			return;
		
		listen += LISTEN_SENSE*o.talk/(dist + 1/MAX_TALK);
	}
	
	// DEBUGGING
	public void printStats() {
		System.out.format("Energy: %.1f \n", energy);
		System.out.format("Thrust: %.1f \n", brain.output(NBrain.THRUST_OUT));
		System.out.format("Turn:   %.1f \n", brain.output(NBrain.TURN_OUT));
	}
	
	public void printBrain() {
		brain.print();
	}
}
