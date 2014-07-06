package bio.organisms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import applet.Config;
import environment.Environment;
import environment.physics.VeryTinyCar;

public class SimpleCircleOrganism extends AbstractOrganism {

	public static final double DEFAULT_MASS = Config.instance.getDouble("SCO_MASS");
	public static final double DEFAULT_RANGE = Config.instance.getDouble("SCO_EFFECT_RANGE");
	public static final double ENERGY_ON_DEATH = Config.instance.getDouble("SCO_ENERGY_ON_DEATH");

	// Graphics
	private static final Color DRAW_COLOR = new Color(.8f, .3f, .2f);

	private VeryTinyCar body;
	/** the "reach" of the organism for attack, mate, touch, etc. */
	private double range;
	private double attackOutput;
	private HashMap<SimpleCircleOrganism,Double> attackers;

	public SimpleCircleOrganism(Environment e, double init_energy, double x, double y) {
		super(e, null, init_energy, x, y);
		body = new VeryTinyCar(DEFAULT_MASS, 0.0, x, y, e.getRandom().nextDouble());
		range = DEFAULT_RANGE;
		attackers = new HashMap<SimpleCircleOrganism,Double>();
	}

	public AbstractOrganism beget(Environment e, Object o) {
		this.useEnergy((this.energy + ENERGY_ON_DEATH)/2, "Child Split");
		SimpleCircleOrganism child = new SimpleCircleOrganism(env, (this.energy - ENERGY_ON_DEATH)/2, x, y);
		child.brain = brain.beget(e, child);
//		this.age = 0;
		return child;
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

	public double getVX(){
		return body.getVelX();
	}
	
	public double getVY(){
		return body.getVelY();
	}
	
	public double getSpeed() {
		return body.getSpeed();
	}

	public double getDirX() {
		return body.getDirX();
	}

	public double getDirY() {
		return body.getDirY();
	}
	
	public void addTurn(double dTurn) {
		body.addTurn(dTurn);
	}
	
	public void setAttackOutput(double attackOutput) {
		this.attackOutput = attackOutput;
	}
	
	public double getAttackOutput() {
		return attackOutput;
	}
	
	public void addAttacker(SimpleCircleOrganism attacker, double attackDamage) {
		attackers.put(attacker,attackDamage);
		this.useEnergy(attackDamage, "Damage");
	}
	
//	public SimpleCircleOrganism getGreatestAttacker() {
//		double maxAttack = 0;
//		SimpleCircleOrganism maxAttacker = null;
//		
//		for(Entry<SimpleCircleOrganism,Double> pair : attackers.entrySet()){
//			if(maxAttacker == null || maxAttack < pair.getValue()) {
//				maxAttack = pair.getValue();
//				maxAttacker = pair.getKey();
//			}
//		}
//		
//		return maxAttacker;
//	}
	
//	public void resolveAttacks() {
//		double damage = 0;
//		for(Double attack : attackers.values()) {
//			damage += attack;
//		}
//		this.useEnergy(damage, "Damage");
//	}
	
	public void clearAttackers() {
		attackers.clear();
	}
	
	@Override
	public void onDeath() {
		// Distribute food to victorious predators.
		double totalDamageThisTurn = 0;
		for(Double damage : attackers.values()) {
			totalDamageThisTurn += damage;
		}
		if(totalDamageThisTurn > 0) {
			for(Entry<SimpleCircleOrganism,Double> entry : attackers.entrySet()) {
				entry.getKey().feed(ENERGY_ON_DEATH*entry.getValue()/totalDamageThisTurn);
			}
		}
		attackers.clear();
	}
}