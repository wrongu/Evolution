

import graphics.RenderPanel;

import javax.swing.JApplet;

public class EvolutionApplet extends JApplet {
	
	private static final long serialVersionUID = 145131501779963654L;
	
	public static final int APPLET_WIDTH = 800, APPLET_HEIGHT = 600;
	
	public void init(){
		setSize(APPLET_WIDTH, APPLET_HEIGHT);
		
		add(new RenderPanel());
		
		setVisible(true);
	}
}
