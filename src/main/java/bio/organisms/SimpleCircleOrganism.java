package bio.organisms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import bio.ann.DumbBrain;
import bio.genetics.Gene;
import bio.organisms.brain.IBrain;
import environment.Environment;
import environment.physics.PointMass;

public class SimpleCircleOrganism extends AbstractOrganism {
	
	private static final Color DRAW_COLOR = new Color(.8f, .3f, .2f);
	private static final Class<? extends IBrain> BRAIN_TYPE = DumbBrain.class;
	
	private Gene<SimpleCircleOrganism> gene;
	
	private PointMass body;
	
	public SimpleCircleOrganism fromGene(Gene<SimpleCircleOrganism> gene, Environment e, double start_energy, double x, double y){
		SimpleCircleOrganism org = gene.create(x, y, e);
		org.gene = gene;
		org.energy = start_energy;
		return org;
	}
	
	public SimpleCircleOrganism newEmpty(Environment e, double start_energy, double x, double y){
		Gene<SimpleCircleOrganism> g = new OrgoGene();
		SimpleCircleOrganism orgo = g.create(x, y, e);
		orgo.gene = g;
		return orgo;
	}
	
	private SimpleCircleOrganism(Environment e, Gene<SimpleCircleOrganism> gene, double init_energy, double x, double y) {
		super(e, gene, init_energy, x, y);
	}

	public AbstractOrganism beget(Environment e) {
		return this.gene.mutate(e.getRandom()).create(pos_x, pos_y, e);
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
	public void preUpdatePhysics() {
		// nothing to do since body is a single pointmass
	}

	@Override
	public void updatePhysics(double dt) {
		// TODO Auto-generated method stub
		this.pos_x = body.getX();
		this.pos_y = body.getY();
	}

	@Override
	public void collide(AbstractOrganism other) {
		if(other instanceof SimpleCircleOrganism){
			this.body.collide(((SimpleCircleOrganism) other).body);
		}
	}
	
	private static class OrgoGene extends Gene<SimpleCircleOrganism>{
		
		private Gene<? extends IBrain> braingene;
		
		public OrgoGene(){
			try {
				braingene = BRAIN_TYPE.newInstance().getGene();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		protected Gene<SimpleCircleOrganism> sub_clone() {
			OrgoGene g = new OrgoGene();
			g.braingene = braingene.clone();
			return g;
		}

		@Override
		protected void sub_mutate(Random r) {
			braingene.mutate(r);
		}

		@Override
		public SimpleCircleOrganism create(double posx, double posy, Environment e) {
			SimpleCircleOrganism orgo = new SimpleCircleOrganism(e, this, 1.0, posx, posy);
			orgo.brain = braingene.create(0, 0, e);
			return orgo;
		}

		@Override
		protected void sub_serialize(DataOutputStream dest) throws IOException {
			braingene.serialize(dest);
		}

		@Override
		protected void sub_deserialize(DataInputStream src) throws IOException {
			braingene.deserialize(src);
		}
		
	}
	
}
