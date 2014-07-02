package applet;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.nio.FloatBuffer;

import environment.Environment;
import environment.RandomFoodEnvironment;
import environment.TestEnvironment;
import environment.TimeVaryingRFE;
import graphics.opengl.RenderGL;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class EvolutionDriver implements Runnable {

	public static final int APPLET_WIDTH = 800, APPLET_HEIGHT = 600;
	public static final int MAX_FPS = 60;
	public static final long TICK_MS = Config.instance.getLong("TICK");

	private Environment env;
	private Canvas canvas;
	// input stuff
	/** four booleans indicating "isDown()" state of up, down, left, and right respectively */
	private boolean[] direction_keys = new boolean[4];
	public static final int UP = 0, DOWN = 1, LEFT = 2, RIGHT = 3;
	/** two integers {dx, dy} of the mouse */
	private int[] mouse_move = new int[2];
	/** single boolean whether mouse is down */
	private int[] mouse_buttons = new int[3];
	// variables for tracking fps
	private long second_timer;
	private int fps, frame_counter;
	private boolean paused, sp_down, mouse_hold, first_frame, shutdown_flag;

	public EvolutionDriver(Canvas c){
		second_timer = 0L;
		fps = MAX_FPS;
		frame_counter = 0;
		paused = true; sp_down = false; mouse_hold = false; first_frame = true; shutdown_flag = false;
		canvas = c;
		
		initEnvironment();
	}

	private void initEnvironment(){
		String type = Config.instance.getString("ENV_TYPE");
		if(type.equals("RandomFoodEnvironment")){
			// initialize everything
			env = new RandomFoodEnvironment(Config.instance.getDouble("ENV_FOOD"), Config.instance.getLong("SEED"));
			// INITIAL POPULATION
			int population = Config.instance.getInt("POPULATION");
			for(int i=0; i<population; i++)
				((RandomFoodEnvironment) env).spawnRandomOrganism();
		} else if (type.equals("TimeVaryingRFE")){
			// initialize everything
			env = new TimeVaryingRFE(Config.instance.getDouble("ENV_FOOD"), Config.instance.getLong("SEED"));
			// INITIAL POPULATION
			int population = Config.instance.getInt("POPULATION");
			for(int i=0; i<population; i++)
				((TimeVaryingRFE) env).spawnRandomOrganism();
		} else if(type.equals("TestEnvironment")){
			env = new TestEnvironment(Config.instance.getLong("SEED"), false);
			((TestEnvironment) env).bindInput(mouse_buttons, mouse_move);
		} else{
			System.err.println("'" + type + "' is not a valid environment name. exiting!");
			System.exit(1);
		}
	}

	public void run(){
		// opengl must be initialized in the same thread where it is used, so we need to create and
		//	add the RenderGL here.
		RenderGL renderpanel = new RenderGL(canvas, env, APPLET_WIDTH, APPLET_HEIGHT);
		renderpanel.bindInputs(direction_keys, mouse_buttons);

		try {
			Mouse.create();
			Keyboard.create();
		}
		catch (LWJGLException e) {
			e.printStackTrace();
		}

		// run simulation
		while(!(Display.isCloseRequested() || shutdown_flag)){
			long now = System.currentTimeMillis();
			checkInput(renderpanel);

			renderpanel.moveCamera();
			renderpanel.redraw();
			updateFPS(now);
			if(first_frame || !paused || (mouse_buttons[0] == 1 && !mouse_hold)){
				first_frame = false;
				mouse_hold = true;
				env.update();
			}
			Display.sync(MAX_FPS);
		}
		renderpanel.destroy();
	}

	public void checkInput(RenderGL renderer){
		// up direction
		if(Keyboard.isKeyDown(Keyboard.KEY_W)) direction_keys[UP] = true;
		else if(Keyboard.isKeyDown(Keyboard.KEY_UP)) direction_keys[UP] = true;
		else direction_keys[UP] = false;
		// down direction
		if(Keyboard.isKeyDown(Keyboard.KEY_S)) direction_keys[DOWN] = true;
		else if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)) direction_keys[DOWN] = true;
		else direction_keys[DOWN] = false;
		// left direction
		if(Keyboard.isKeyDown(Keyboard.KEY_A)) direction_keys[LEFT] = true;
		else if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) direction_keys[LEFT] = true;
		else direction_keys[LEFT] = false;
		// right direction
		if(Keyboard.isKeyDown(Keyboard.KEY_D)) direction_keys[RIGHT] = true;
		else if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) direction_keys[RIGHT] = true;
		else direction_keys[RIGHT] = false;
		// PAUSE ON SPACE
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
			if(!sp_down) paused = !paused;
			sp_down = true;
		} else{
			sp_down = false;
		}

		// mouse movement
		FloatBuffer mouseCoords = renderer.screenToWorldCoordinates(Mouse.getX(), Mouse.getY());
		mouse_move[0] = (int) mouseCoords.get();
		mouse_move[1] = (int) mouseCoords.get();
		if(Mouse.isButtonDown(0)) mouse_buttons[0] = 1;
		else mouse_buttons[0] = 0;
		mouse_buttons[1] = Mouse.getDWheel();

		if(mouse_buttons[0] != 1)
			mouse_hold = false;
	}
	
	private void updateFPS(long now){
		frame_counter++;
		if(now - second_timer > 1000){
			fps = frame_counter;
			frame_counter = 0;
			second_timer = now;
			//System.out.println(fps);
		}
	}
	
	private class WindowHandler implements WindowListener{

		public void windowClosing(WindowEvent event) {
			shutdown_flag = true;
		}

		public void windowDeactivated(WindowEvent event) {
			if(Config.instance.getBoolean("PAUSE_ON_CLOSE")) paused = true;
		}

		public void windowIconified(WindowEvent event) {
			if(Config.instance.getBoolean("PAUSE_ON_CLOSE")) paused = true;
		}

		public void windowClosed(WindowEvent event) {}
		public void windowOpened(WindowEvent event) {}
		public void windowActivated(WindowEvent event) {}
		public void windowDeiconified(WindowEvent event) {}
		
	}

	public static void main(String[] args){
		// create JFrame with a canvas
		JFrame window = new JFrame("Evolution App");
		window.setSize(APPLET_WIDTH, APPLET_HEIGHT);
		Canvas canvas = new Canvas();
		canvas.setPreferredSize(new Dimension(APPLET_WIDTH, APPLET_HEIGHT));
		window.getContentPane().add(canvas);
		window.setIgnoreRepaint(true);
		window.pack();
		window.setVisible(true);

		EvolutionDriver driver = new EvolutionDriver(canvas);
		window.addWindowListener(driver.new WindowHandler());
		driver.run();
		window.dispose();
	}
}