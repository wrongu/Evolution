package structure;

import java.awt.Color;
import java.awt.Graphics2D;

import graphics.IDrawable;

public class Link extends Muscle implements IDrawable {
	
	private double mass;
	private double x1, y1, x2, y2;
	
	public Link(double rest_length, double mass){
		super(rest_length);
		x1 = y1 = y2 = .0;
		x2 = rest_length;
		this.mass = mass;
	}
	
	public void draw(Graphics2D g) {
		g.setColor(Color.WHITE);
		g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
	}

	@Override
	public void setMovement(double target, double strength) {
		super.setMovement(target, strength);
	}
}
