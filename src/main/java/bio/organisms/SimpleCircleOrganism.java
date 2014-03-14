package bio.organisms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

import bio.organisms.brain.ISense;
import bio.organisms.brain.IOutput;
import environment.Environment;
import environment.physics.PointMass;

// DRAWING
import static org.lwjgl.opengl.GL11.*;

public class SimpleCircleOrganism extends AbstractOrganism {

	public static final double ENERGY_PER_OOMPH = 0.1;
	public static final double ENERGY_PER_TURN = 0.1;
	public static final double ENERGY_PER_CHATTER = 0.01;
	public static final double ENERGY_PER_ATTACK = 0.5;
	public static final double MITOSIS_THRESHOLD = 0.5;

	private static final Color DRAW_COLOR = new Color(.8f, .3f, .2f);
	private static final double DRAW_SMOOTHNESS = 10;

	private PointMass body;
	/** orientation in radians. zero is along positive x. */
	private double direction;
	/** effort exerted to move forward */
	private double oomph;
	/** current rotational speed */
	private double omega;
	/** effort exerted to turn */
	private double twist;

	public SimpleCircleOrganism(Environment e, double init_energy, double x, double y) {
		super(e, null, init_energy, x, y);
		body = new PointMass(5.0);
		body.initPosition(x, y);
		direction = e.getRandom().nextDouble()*Math.PI*2;
		oomph = omega = twist = 0.;
	}

	public AbstractOrganism beget(Environment e) {
		return this.gene.mutate(e.getRandom()).create(pos_x, pos_y, e);
	}

	protected List<ISense> createSenses(){
		return Arrays.asList(new Listen(), new SpeedSense(), new TurnSense(), new EnergySense());
	}

	protected List<IOutput> createOutputs(){
		return Arrays.asList(new Accelerate(), new Twist(), new Mitosis());
	}

	@Override
	public void draw(Graphics2D g, float sx, float sy, float scx, float scy){
		g.setColor(DRAW_COLOR);
		int diam = (int) (2.*body.getRadius());
		int x = (int) ((sx + pos_x) * scx);
		int y = (int) ((sy + pos_y) * scy);
		g.drawOval(x, y, diam, diam);
		g.setColor(Color.WHITE);
		int vx = (int) (body.getVX() * scx), vy = (int) (body.getVY() * scy);
		g.drawLine(x, y, x-vx, y-vy);
	}

	@Override
	public void glDraw(){
		glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		double dx = Math.cos(direction);
		double dy = Math.sin(direction);
		double vx = body.getVX();
		double vy = body.getVY();
		glBegin(GL_QUADS);
		{
			glVertex2d(pos_x + 2*dx, pos_y + 2*dy);
			glVertex2d(pos_x + 2*dy, pos_y - 2*dx);
			glVertex2d(pos_x - 2*vx - 2*dx, pos_y - 2*vy - 2*dy);
			glVertex2d(pos_x - 2*dx, pos_y + 2*dx);
		}
		glEnd();
		double range = body.getRadius();
		int n = (int)(range*DRAW_SMOOTHNESS);
		double t = 2*Math.PI/(double)n;
		glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
		glBegin(GL_LINES);
		{
			for(int i = 0; i < n; i++) {
				glVertex2d(pos_x + range*Math.cos(i*t), pos_y + range*Math.sin(i*t) );
				glVertex2d(pos_x + range*Math.cos((i+1)*t), pos_y + range*Math.sin((i+1)*t));
			}
		}
		glEnd();
	}

	@Override
	public void preUpdatePhysics() {
		// TODO factor out spinning point physics
		// rotatinoal movement update
		direction += omega;
		omega += twist;
		omega *= 0.8; // rotational viscosity
		// linear movement update
		this.body.addForce(oomph * Math.cos(direction), oomph * Math.sin(direction));
	}

	@Override
	public void updatePhysics(double dt) {
		this.body.move(env, dt);
		this.pos_x = body.getX();
		this.pos_y = body.getY();
	}

	@Override
	public void collide(AbstractOrganism other) {
		if(other instanceof SimpleCircleOrganism){
			this.body.collide(((SimpleCircleOrganism) other).body);
		}
	}

	// SENSES
	private class Listen implements ISense{
		public double doSense(Environment e, AbstractOrganism o) {
			// TODO query environment for nearby organisms
			return 0;
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
			super(SimpleCircleOrganism.this, ENERGY_PER_OOMPH);
		}
		@Override
		protected void sub_act(double energy) {
			SimpleCircleOrganism.this.oomph = energy / ENERGY_PER_OOMPH;
		}
	}
	private class Twist extends IOutput{
		public Twist() {
			super(SimpleCircleOrganism.this, ENERGY_PER_TURN);
		}
		@Override
		protected void sub_act(double energy) {
			SimpleCircleOrganism.this.twist = energy / ENERGY_PER_TURN;
		}
	}
	private class Mitosis extends IOutput{
		public Mitosis() {
			super(SimpleCircleOrganism.this, 1.0);
		}
		@Override
		protected void sub_act(double energy) {
			if(energy > MITOSIS_THRESHOLD){
				env.organisms.add(beget(env));
			}
		}
	}
	private class Chatter extends IOutput{
		public Chatter() {
			super(SimpleCircleOrganism.this, ENERGY_PER_CHATTER);
		}
		@Override
		protected void sub_act(double energy) {
			// TODO
		}
	}
	private class Attack extends IOutput{
		public Attack() {
			super(SimpleCircleOrganism.this, ENERGY_PER_ATTACK);
		}
		@Override
		protected void sub_act(double energy) {
			// TODO
		}
	}
}