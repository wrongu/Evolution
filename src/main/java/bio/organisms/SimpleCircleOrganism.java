package bio.organisms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import bio.organisms.brain.ISense;
import bio.organisms.brain.IOutput;
import environment.Environment;

import environment.physics.VeryTinyCar;
// DRAWING
import static org.lwjgl.opengl.GL11.*;

public class SimpleCircleOrganism extends AbstractOrganism {
	
	public static final double DEFAULT_MASS = 5.0;
	public static final double DEFAULT_RANGE = 10;

	public static final double ENERGY_PER_OOMPH = 0.1;
	public static final double ENERGY_PER_TURN = 0.5;
	public static final double ENERGY_PER_CHATTER = 0.01;
	public static final double CHATTER_RANGE = 300;
	public static final double ENERGY_PER_ATTACK = 0.5;
	public static final double MITOSIS_THRESHOLD = 0.97;

	private static final Color DRAW_COLOR = new Color(.8f, .3f, .2f);
	private static final double DRAW_SMOOTHNESS = 10;
	private static final double TAIL_LENGTH_PER_SPEED = 0.3;
	
	// Turning directions
	private static enum DIRECTION {CW, CCW};

	private VeryTinyCar body;
	/** the "reach" of the organism for attack, mate, touch, etc. */
	private double range;
//	/** orientation in radians. zero is along positive x. */
//	private double direction;
	/** effort exerted to move forward */
	private double oomph;
	/** current rotational speed */
	private double omega;
	/** effort exerted to turn */
	private double twist_ccw, twist_cw;
	/** chatter signal strength */
	private double chatter;

	public SimpleCircleOrganism(Environment e, double init_energy, double x, double y) {
		super(e, null, init_energy, x, y);
//		direction = e.getRandom().nextDouble()*Math.PI*2;
		body = new VeryTinyCar(DEFAULT_MASS, x, y, e.getRandom().nextDouble());
		oomph = omega = twist_ccw = twist_cw = 0.;
		range = DEFAULT_RANGE;
	}

	public AbstractOrganism beget(Environment e, Object o) {
		this.useEnergy(this.energy / 2.0, "Child Split");
		AbstractOrganism child = new SimpleCircleOrganism(env, energy, body.getPosX(), body.getPosY());
		child.brain = brain.beget(e, child);
		return child;
	}

	protected List<ISense> createSenses(){
		return Arrays.asList(new Listen(), new SpeedSense(), new TurnSense(), new EnergySense());
	}

	protected List<IOutput> createOutputs(){
		return Arrays.asList(new Accelerate(), new Twist(DIRECTION.CW), new Twist(DIRECTION.CCW)/*, new Mitosis(), new Chatter()*/);
	}

	@Override
	public void draw(Graphics2D g, float sx, float sy, float scx, float scy){
		g.setColor(DRAW_COLOR);
		int diam = (int) (2.*range);
		int x = (int) ((sx + body.getPosX()) * scx);
		int y = (int) ((sy + body.getPosY()) * scy);
		g.drawOval(x, y, diam, diam);
		g.setColor(Color.WHITE);
		double[] vel = body.getVel();
		int vx = (int) (vel[0] * scx), vy = (int) (vel[1] * scy);
		g.drawLine(x, y, x-vx, y-vy);
	}

	@Override
	public void glDraw(){
		float r = (float) Math.exp(-energy);
		float g = 1-r;
		glColor4f(r, g, 0f, 1.0f);
		double[] pos = body.getPos();
		double[] d = body.getDir();
//		double dx = Math.cos(direction);
//		double dy = Math.sin(direction);
		double tail = TAIL_LENGTH_PER_SPEED*Math.max(body.getSpeed(),0);
//		double vx = body.getVX();
//		double vy = body.getVY();
		glBegin(GL_QUADS);
		{
			glVertex2d(pos[0] + 2*d[0], pos[1] + 2*d[1]);
			glVertex2d(pos[0] + 2*d[1], pos[1] - 2*d[0]);
			glVertex2d(pos[0] - 2*(tail + 1)*d[0], pos[1] - 2*(tail + 1)*d[1]);
			glVertex2d(pos[0] - 2*d[1], pos[1] + 2*d[0]);
		}
		glEnd();
		int n = (int)(range*DRAW_SMOOTHNESS);
		double t = 2*Math.PI/(double)n;
		glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
		glBegin(GL_LINES);
		{
			for(int i = 0; i < n; i++) {
				glVertex2d(pos[0] + range*Math.cos(i*t), pos[1] + range*Math.sin(i*t) );
				glVertex2d(pos[0] + range*Math.cos((i+1)*t), pos[1] + range*Math.sin((i+1)*t));
			}
		}
		glEnd();
	}
	
	@Override
	public void preUpdatePhysics() {
		// TODO factor out spinning point physics
		// rotational movement update
//		direction += omega;
//		omega += (twist_ccw - twist_cw);
//		omega *= 0.8; // rotational viscosity
//		// linear movement update
//		oomph *= 0.001; // TESTING
//		this.body.addForce(oomph * Math.cos(direction), oomph * Math.sin(direction));
	}
	
	@Override
	public void updatePhysics(double dt) {
		this.body.update(dt);
//		double[] pos = body.getPos();
//		this.pos_x = pos[0];
//		this.pos_y = pos[1];
	}

	@Override
	public void collide(AbstractOrganism other) {
//		if(other instanceof SimpleCircleOrganism){
//			this.body.collide(((SimpleCircleOrganism) other).body);
//		}
	}
	
	public void addExternalForce(double fx, double fy){
		this.body.addForce(new double[]{fx, fy});
	}
	
	public double getX() {
		return body.getPosX();
	}
	
	public double getY() {
		return body.getPosY();
	}

	// SENSES
	private class Listen implements ISense{
		public double doSense(Environment e, AbstractOrganism o) {
			// query environment for nearby organisms
			HashSet<AbstractOrganism> talkers = e.getNearby(o, CHATTER_RANGE, true);
			double signal = 0;
			for(AbstractOrganism orgo : talkers) {
				if(orgo instanceof SimpleCircleOrganism) {
					double r = Math.hypot(orgo.getX() - SimpleCircleOrganism.this.getX(), orgo.getY() - SimpleCircleOrganism.this.getY());
					signal += (r <= CHATTER_RANGE) ? ((SimpleCircleOrganism)orgo).chatter/(1 + r) : 0;
				}
			}
			return signal;
		}
	}
	private class SpeedSense implements ISense{
		public double doSense(Environment e, AbstractOrganism o) {
			return SimpleCircleOrganism.this.body.getSpeed();
		}
	}
	private class TurnSense implements ISense{
		public double doSense(Environment e, AbstractOrganism o) {
			return SimpleCircleOrganism.this.omega;
		}
	}
	private class EnergySense implements ISense{
		public double doSense(Environment e, AbstractOrganism o) {
			return SimpleCircleOrganism.this.energy;
		}
	}
	// OUTPUTS
	private class Accelerate extends IOutput{
		public Accelerate() {
			super(SimpleCircleOrganism.this, ENERGY_PER_OOMPH, "Accelerate");
		}
		@Override
		protected void sub_act(double energy) {
			SimpleCircleOrganism.this.body.addThrust(energy / ENERGY_PER_OOMPH);
		}
	}
	private class Twist extends IOutput{
		
		private DIRECTION dir;
		
		public Twist(DIRECTION dir) {
			super(SimpleCircleOrganism.this, ENERGY_PER_TURN, "Turn");
			this.dir = dir;
		}
		@Override
		protected void sub_act(double energy) {
			switch(this.dir){
			case CW:
				SimpleCircleOrganism.this.body.addTurn(-energy / ENERGY_PER_TURN);
				break;
			case CCW:
				SimpleCircleOrganism.this.body.addTurn(energy / ENERGY_PER_TURN);
				break;
			}
		}
	}
	private class Mitosis extends IOutput{
		public Mitosis() {
			super(SimpleCircleOrganism.this, 1.0, "Mitosis");
		}
		@Override
		protected void sub_act(double energy) {
			if(energy > MITOSIS_THRESHOLD){
				env.addOrganism(beget(env, null));
			}
		}
	}
	private class Chatter extends IOutput{
		public Chatter() {
			super(SimpleCircleOrganism.this, ENERGY_PER_CHATTER, "Chatter");
		}
		@Override
		protected void sub_act(double energy) {
			SimpleCircleOrganism.this.chatter = energy / ENERGY_PER_CHATTER;
		}
	}
	private class Attack extends IOutput{
		public Attack() {
			super(SimpleCircleOrganism.this, ENERGY_PER_ATTACK, "Attack");
		}
		@Override
		protected void sub_act(double energy) {
			// TODO
		}
	}
	
}