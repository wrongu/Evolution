package graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class RenderPanel extends JPanel {

	private static final long serialVersionUID = -1902890242410191035L;
	
	public final Color BACKGROUND_COLOR = Color.BLACK;
	
	private IDrawable render;
	
	public RenderPanel(IDrawable d){
		render = d;
		
		Thread updater = new UpdateThread();
		updater.setDaemon(true);
		updater.start();
	}

	@Override
	public void paintComponent(Graphics g){
		background(g);
		render.draw((Graphics2D) g);
	}
	
	private void background(Graphics g){
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0,0,this.getWidth(), this.getHeight());
	}
	
	private class UpdateThread extends Thread{
		public void run(){
			while(true) repaint();
		}
	}
}
