

import environment.Environment;
import graphics.DebugGenes;
import graphics.RenderPanel;

import javax.swing.JApplet;

//import ann.DrawGraph;

public class EvolutionApplet extends JApplet {
	
	private static final long serialVersionUID = 145131501779963654L;
	public static final int APPLET_WIDTH = 800, APPLET_HEIGHT = 600;
	
	private Environment env;
	
	public void init(){
		setSize(APPLET_WIDTH, APPLET_HEIGHT);
		
//		add(new RenderPanel(new DrawGraph()));
		env = new Environment(APPLET_WIDTH, APPLET_HEIGHT);
		add(new RenderPanel(env));
//		add(new DebugGenes());
		
		Thread th = new Thread(env);
		th.setDaemon(true);
		th.start();
		
		setVisible(true);
	}
}
