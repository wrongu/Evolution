

import environment.Environment;
import graphics.DebugGenes;
import graphics.DrawGraph;
import graphics.RenderPanel;
import graphics.RenderPanelGL;

import javax.swing.JApplet;
import javax.swing.JPanel;


//import ann.DrawGraph;

public class EvolutionApplet extends JApplet {
	
	private static final long serialVersionUID = 145131501779963654L;
	public static final int APPLET_WIDTH = 800, APPLET_HEIGHT = 600;
	
	private Environment env;
	
	public void init(){
		setSize(APPLET_WIDTH, APPLET_HEIGHT);
		
//		add(new RenderPanel(new DrawGraph()));
//	add(new DebugGenes());
		
		env = new Environment(APPLET_WIDTH, APPLET_HEIGHT);
		JPanel renderpanel = new RenderPanelGL(); 
		add(renderpanel);

		setVisible(true);
		
		((RenderPanelGL) renderpanel).initialize(env, APPLET_WIDTH, APPLET_HEIGHT);
		
		Thread th = new Thread(env);
		th.setDaemon(true);
		th.start();
		
	}
}