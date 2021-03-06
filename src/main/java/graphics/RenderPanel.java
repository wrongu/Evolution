package graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import environment.TestEnvironment;

import javax.swing.JPanel;

public class RenderPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {

	private static final long serialVersionUID = -1902890242410191035L;

	public static final Color BACKGROUND_COLOR = Color.BLACK;
	public static final Color ORGANISM_COLOR = new Color(0F, 1F, 1F); 

	private IDrawable render;
	PointerInfo pointerInfo = MouseInfo.getPointerInfo();
	private boolean mouseIsDown;
	private boolean spaceIsDown;
	private int mx, my;

	public RenderPanel(IDrawable d){
		render = d;

		mouseIsDown = false;
		spaceIsDown = false;
		addMouseListener(this);
		addKeyListener(this);

		Thread updater = new UpdateThread();
		updater.setDaemon(true);
		updater.start();
	}

	@Override
	public void paintComponent(Graphics g){
		background(g);
		g.setColor(Color.WHITE);
		render.draw((Graphics2D) g, 0f, 0f, 1f, 1f);
	}

	private void background(Graphics g){
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0,0,this.getWidth(), this.getHeight());
		g.setColor(Color.GRAY);
		g.drawRect(0,0,this.getWidth(), this.getHeight());
	}

	private class UpdateThread extends Thread{

		public void run(){
			while(true){
				if(render instanceof TestEnvironment){
					if(spaceIsDown) ((TestEnvironment) render).space_press(spaceIsDown);
				}
				repaint();
			}
		}
	}

	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mouseDragged(MouseEvent arg0) {}

	public void mousePressed(MouseEvent arg0) {
		mouseIsDown = true;
		mx = arg0.getX();
		my = arg0.getY();
	}

	public void mouseReleased(MouseEvent arg0) {
		mouseIsDown = false;
	}

	public void mouseMoved(MouseEvent arg0) {
		mx = arg0.getX();
		my = arg0.getY();
	}
	
	public void keyPressed(KeyEvent arg0) {
//		if(arg0.getKeyCode() == KeyEvent.VK_SPACE) {
//			((Environment) render).space_press(true);
//			System.out.println("Spacebar pressed.");
//		}
		System.out.println("Pressed " + arg0.getKeyCode());
	}
	
	public void keyReleased(KeyEvent arg0) {
		if(arg0.getKeyCode() == KeyEvent.VK_SPACE && render instanceof TestEnvironment)
			((TestEnvironment) render).space_press(false);
	}
	
	public void keyTyped(KeyEvent arg0) {}
}
