

import java.awt.Canvas;
import java.awt.Container;
import java.awt.Dimension;

import environment.Environment;
import graphics.DebugGenes;
import graphics.DrawGraph;
import graphics.RenderPanel;
import graphics.RenderGL;

import javax.swing.JApplet;
import javax.swing.JPanel;


//import ann.DrawGraph;

public class EvolutionApplet extends JApplet implements Runnable {
	
	private static final long serialVersionUID = 145131501779963654L;
	public static final int APPLET_WIDTH = 800, APPLET_HEIGHT = 600;
	
	private Environment env;
	private Canvas canvas;
	
	public void init(){
		setSize(APPLET_WIDTH, APPLET_HEIGHT);
		
		canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(APPLET_WIDTH, APPLET_HEIGHT));
		
		getContentPane().add(canvas);
		
		setVisible(true);
		
		Thread th = new Thread(this);
		th.setDaemon(true);
		setIgnoreRepaint(true);
		th.start();
		
	}
	
	public void run(){
		// initialize everything
		env = new Environment(APPLET_WIDTH, APPLET_HEIGHT);
		// opengl must be initialized in the same thread where it is used, so we need to create and
		//	add the RenderGL here.
		RenderGL renderpanel = new RenderGL(canvas, env, APPLET_WIDTH, APPLET_HEIGHT);
		
		// run simulation
		while(true){
			env.update();
			renderpanel.redraw();
		}
	}
}