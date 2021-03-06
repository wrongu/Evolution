package graphics;

import java.awt.Graphics2D;

public interface IDrawable {
	/**
	 * Render this object on a 2D graphics canvas
	 * @param g the graphics object to draw to
	 */
	public void draw(Graphics2D g, float shift, float shifty, float scalex, float scaley);
}
