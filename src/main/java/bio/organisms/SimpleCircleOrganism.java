package bio.organisms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import applet.Config;
import bio.organisms.brain.ISense;
import bio.organisms.brain.IOutput;
import environment.Environment;
import environment.physics.VeryTinyCar;
// DRAWING
import static org.lwjgl.opengl.GL11.*;

public class SimpleCircleOrganism extends AbstractOrganism {

	public static final double DEFAULT_MASS = Config.instance.getDouble("SCO_MASS");
	public static final double DEFAULT_RANGE = Config.instance.getDouble("SCO_EFFECT_RANGE");

	// Energy constants for actions
	public static final double ENERGY_PER_OOMPH = Config.instance.getDouble("SCO_ENERGY_PER_OOMPH");
	public static final double ENERGY_PER_TURN = Config.instance.getDouble("SCO_ENERGY_PER_TURN");
	public static final double ENERGY_PER_CHATTER = Config.instance.getDouble("SCO_ENERGY_PER_CHATTER");
	public static final double CHATTER_RANGE = Config.instance.getDouble("SCO_CHATTER_RANGE");
	public static final double ENERGY_PER_ATTACK = Config.instance.getDouble("SCO_ENERGY_PER_ATTACK");
	public static final double MITOSIS_THRESHOLD = Config.instance.getDouble("SCO_MITOSIS_THRESHOLD");
	public static final double ENERGY_ON_DEATH = Config.instance.getDouble("SCO_ENERGY_ON_DEATH");

	// Action strengths.
	public static final double OOMPH_STRENGTH = Config.instance.getDouble("SCO_OOMPH_STRENGTH");
	public static final double TURN_STRENGTH = Config.instance.getDouble("SCO_TURN_STRENGTH");
	public static final double CHATTER_STRENGTH = Config.instance.getDouble("SCO_CHATTER_STRENGTH");
	public static final double ATTACK_STRENGTH = Config.instance.getDouble("SCO_ATTACK_STRENGTH");

	// Sense sensitivities.
	public static final double SPEED_SENSITIVITY = Config.instance.getDouble("SCO_SPEED_SENSITIVITY");
	public static final double TURN_SENSITIVITY = Config.instance.getDouble("SCO_TURN_SENSITIVITY");
	public static final double LISTEN_SENSITIVITY = Config.instance.getDouble("SCO_LISTEN_SENSITIVITY");
	public static final double ENERGY_SENSITIVITY = Config.instance.getDouble("SCO_ENERGY_SENSITIVITY");
	public static final double TOUCH_SENSITIVITY = Config.instance.getDouble("SCO_TOUCH_SENSITIVITY");

	// Graphics
	private static final Color DRAW_COLOR = new Color(.8f, .3f, .2f);
	private static final double DRAW_SMOOTHNESS = 10;
	private static final double TAIL_LENGTH_PER_SPEED = 0.3;

	// Turning directions
	private static enum DIRECTION {CW, CCW};

	private VeryTinyCar body;
	/** the "reach" of the organism for attack, mate, touch, etc. */
	private double range;
	/** chatter signal strength */
	private double chatter;

	public SimpleCircleOrganism(Environment e, double init_energy, double x, double y) {
		super(e, null, init_energy, x, y);
		body = new VeryTinyCar(DEFAULT_MASS, 0.0, x, y, e.getRandom().nextDouble());
		range = DEFAULT_RANGE;
	}

	public AbstractOrganism beget(Environment e, Object o) {
		this.useEnergy((this.energy + ENERGY_ON_DEATH)/2, "Child Split");
		AbstractOrganism child = new SimpleCircleOrganism(env, (this.energy - ENERGY_ON_DEATH)/2, x, y);
		child.brain = brain.beget(e, child);
		return child;
	}

	protected List<ISense> createSenses(){
		return Arrays.asList(/*new Listen(),*/ new SpeedSense(), new TurnSense(), new EnergySense());
	}

	protected List<IOutput> createOutputs(){
		return Arrays.asList(new Accelerate(), new Twist(DIRECTION.CW), new Twist(DIRECTION.CCW), new Attack(), new Mitosis() /*,new Chatter()*/);
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
	public void preUpdatePhysics() {
		// nothing to be done since VeryTinyCar handles it all
	}

	@Override
	public void updatePhysics(double dt) {
		this.body.update(dt);
		x = body.getPosX();
		y = body.getPosY();
	}

	@Override
	public void collide(AbstractOrganism other) {
		if(other instanceof SimpleCircleOrganism){
			this.body.collide(((SimpleCircleOrganism) other).body);
		}
	}

	public void addExternalForce(double fx, double fy){
		this.body.addForce(new double[]{fx, fy});
	}

//	public double getX() {
//		return body.getPosX();
//	}
//
//	public double getY() {
//		return body.getPosY();
//	}

	// SENSES
	private class Listen implements ISense{
		public double doSense(Environment e, AbstractOrganism o) {
			// query environment for nearby organisms
			LinkedList<AbstractOrganism> talkers = e.getInDisk(o.getX(), o.getY(), CHATTER_RANGE);
			talkers.remove(o);
			
			double signal = 0;
			for(AbstractOrganism orgo : talkers) {
				if(orgo instanceof SimpleCircleOrganism) {
					double r = Math.hypot(orgo.getX() - SimpleCircleOrganism.this.getX(), orgo.getY() - SimpleCircleOrganism.this.getY());
					signal += (r <= CHATTER_RANGE) ? ((SimpleCircleOrganism)orgo).chatter/(1 + r) : 0;
				}
			}
			return signal*LISTEN_SENSITIVITY;
		}
	}
	private class SpeedSense implements ISense{
		public double doSense(Environment e, AbstractOrganism o) {
			return SimpleCircleOrganism.this.body.getSpeed() * SPEED_SENSITIVITY;
		}
	}
	private class TurnSense implements ISense{
		public double doSense(Environment e, AbstractOrganism o) {
			return SimpleCircleOrganism.this.body.getTurn() * TURN_SENSITIVITY;
		}
	}
	private class EnergySense implements ISense{
		public double doSense(Environment e, AbstractOrganism o) {
			return SimpleCircleOrganism.this.energy * ENERGY_SENSITIVITY;
		}
	}
	// OUTPUTS
	private class Accelerate extends IOutput{
		public Accelerate() {
			super(SimpleCircleOrganism.this, ENERGY_PER_OOMPH, "Accelerate");
		}
		@Override
		protected void sub_act(double energy) {
			SimpleCircleOrganism.this.body.addThrust(energy * OOMPH_STRENGTH / ENERGY_PER_OOMPH);
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
				SimpleCircleOrganism.this.body.addTurn(-energy * TURN_STRENGTH / ENERGY_PER_TURN);
				break;
			case CCW:
				SimpleCircleOrganism.this.body.addTurn(energy * TURN_STRENGTH / ENERGY_PER_TURN);
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
				System.out.println("mitosis: "+energy);
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
			SimpleCircleOrganism.this.chatter = energy * CHATTER_STRENGTH / ENERGY_PER_CHATTER;
		}
	}
	private class Attack extends IOutput{
		public Attack() {
			super(SimpleCircleOrganism.this, ENERGY_PER_ATTACK, "Attack");
		}
		@Override // Naive implementation:
		protected void sub_act(double energy) {
			// Iterate through all organisms in the disk.
			for(AbstractOrganism o : env.getInDisk(x, y, DEFAULT_RANGE)) {
				SimpleCircleOrganism sco = (SimpleCircleOrganism)o;
				// Skip over itself and dead organisms.
				if(sco == SimpleCircleOrganism.this || !sco.is_alive()) {
					continue;
				}
				double damage = energy*ATTACK_STRENGTH/ENERGY_PER_ATTACK;
				sco.useEnergy(damage,"ATTACK_DAMAGE");
				if(!sco.is_alive()) {
					SimpleCircleOrganism.this.feed(ENERGY_ON_DEATH);
				}
			}
		}
	}

}