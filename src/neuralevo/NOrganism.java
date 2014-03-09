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
	public static final double RESISTANCE = 0.07;
//	public static final double NORMAL_RESISTANCE_FACTOR = 0.5;
	public static final double DEFAULT_MASS = 1;
	public static final double RANGE = 10;
	public static final double LISTEN_RANGE = 500;
	public static final double MAX_TALK = 1;
	
	// Food and energy constants.
	public static final double FOOD_ON_DEATH = 2;
	public static final double CONSUMPTION_ROOT = 2; // 2 -> sqrt curve, 3 -> 3rd root curve, so on.
	public static final double CONSUMPTION_CURVATURE = 1; // larger means more abrupt consumption curve.
	public static final double ATTACK_COST = 0.07;
	public static final double TALK_COST = 0.0001;
	public static final double THRUST_COST = 0.0005;
	public static final double TURN_COST = 0.0005;
	public static final double MATING_ENERGY_MULTIPLIER = 0.75; // Parents give this proportion of energy to offspring.
	public static final double MATING_COST = 0.5; // Take this proportion from parents to mate (after birth).
	
	// Action constants.
	public static final double THRUST_STRENGTH = 0.05;
	public static final double TURN_STRENGTH = 0.1;
	public static final double ATTACK_STRENGTH = 0.2;
	public static final double TALK_STRENGTH = 0.5;
	public static final double MATING_RATE = 0.1;
	
	// Sensory constants.
	public static final double THRUST_SENSE = 1/THRUST_STRENGTH;
	public static final double TURN_SENSE = 1/TURN_STRENGTH;
	public static final double LISTEN_SENSE = 0.5;
	public static final double ENERGY_SENSE = 1;
	public static final double TOUCH_SENSE = 0.5;
	
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
	private double mate;
	private double touch;
	private double attack;
	
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
		
		eat(e);
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
	
	public void updateActions() {
		// Thrust action
		double thrustOut = brain.output(NBrain.THRUST_OUT);
		energy -= THRUST_COST*Math.abs(thrustOut);
		tacc += THRUST_STRENGTH*thrustOut;

		// Turn action
		double turnOut = brain.output(NBrain.TURN_OUT);
		energy -= TURN_COST*Math.abs(turnOut);
		turn = TURN_STRENGTH*turnOut;
		
		// Talk action
		double talkOut = brain.output(NBrain.TALK);
		energy -= TALK_COST*Math.abs(talkOut);
		talk = TALK_STRENGTH*talkOut;
		
		// Set attack value.
		attack = brain.output(NBrain.ATTACK);
		attack = attack > 0 ? attack : 0;
		attack *= ATTACK_STRENGTH;
		
		// Set mate value.
		mate = brain.output(NBrain.MATE);
		mate = mate < 0 ? 0 : (mate > 1 ? 1 : mate); // Probability cutoff.
	}
	
	public void updateSenses() {
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
		
		// Update touch.
		brain.input(NBrain.TOUCH, TOUCH_SENSE*touch);
		touch = 0;
		
	}
	
	public void touching(NOrganism o) {
		double dx = pos.x - o.pos.x;
		double dy = pos.y - o.pos.y;
		double dist = Math.hypot(dx, dy);
		
		if(dist <= RANGE) 
			touch++;
	}
	
	public void attack(NOrganism o) {
		// Determine if in range.
		double dx = pos.x - o.pos.x;
		double dy = pos.y - o.pos.y;
		double dist = Math.hypot(dx, dy);
		
		if(dist > RANGE) 
			return;
		
		// Attack o.
		if(o.isAlive()) {
			o.energy -= attack;
			if(!o.isAlive()) {
				eat(FOOD_ON_DEATH);
			}
		}
	}
	
	public void listenTo(NOrganism o) {
		double dx = pos.x - o.pos.x;
		double dy = pos.y - o.pos.y;
		double dist = Math.hypot(dx, dy);
		
		if(dist > LISTEN_RANGE)
			return;
		
		listen += LISTEN_SENSE*o.talk/(dist + 1/MAX_TALK);
	}
	
	public NOrganism mateWith(NOrganism o) {
		// In range?
		double dx = pos.x - o.pos.x;
		double dy = pos.y - o.pos.y;
		double dist = Math.hypot(dx, dy);
		
		if(dist > RANGE)
			return null;
		
		// Mating probability? Does it occur?
		double prob = MATING_RATE*mate*o.mate;
		if(Math.random() >= prob) {
			return null;
		}
		
		// So mating has commenced. Time to charge the mating tax and determine energies.
		energy -= MATING_COST;
		o.energy -= MATING_COST;
		double offspringEnergy = (energy + o.energy)*(1-MATING_ENERGY_MULTIPLIER);
		energy *= (MATING_ENERGY_MULTIPLIER);
		o.energy *= (MATING_ENERGY_MULTIPLIER);
		
		// Time to create some life.
		NGene offspringGene = gene.cross(o.gene);
		offspringGene.mutate();
		// Initial position.
		Vector2d initPos = new Vector2d(pos);
		initPos.add(o.pos);
		initPos.scale(0.5);
		// Initial velocity.
		Vector2d initVel = new Vector2d(vel);
		initVel.add(o.vel);
		initVel.scale(0.5);
		NOrganism offspring = new NOrganism(offspringGene, offspringEnergy,initPos, initVel);
		
		return offspring;
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
