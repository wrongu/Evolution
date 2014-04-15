package applet;
import java.awt.Canvas;
import java.awt.Dimension;
import java.nio.FloatBuffer;

import environment.Environment;
import environment.TestEnvironment;
import graphics.opengl.RenderGL;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

//import ann.DrawGraph;

public class EvolutionDriver implements Runnable {

	public static final int APPLET_WIDTH = 800, APPLET_HEIGHT = 600;
	public static final int MAX_FPS = 60;
	public static final long TICK_MS = 100;

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
	private boolean paused, sp_down, mouse_hold, first_frame;

	public EvolutionDriver(Canvas c){

		second_timer = 0L;
		fps = MAX_FPS;
		frame_counter = 0;
		paused = true; sp_down = false; mouse_hold = false; first_frame = true;

		initEnvironment();
	}

	private void initEnvironment(){

		//		// initialize everything
		//		env = new RandomFoodEnvironment(1.0, 0L);
		//		// INITIAL POPULATION
		//		for(int i=0; i<10; i++){
		//			double x = 60. * Math.cos(2*Math.PI*i/10.);
		//			double y = 60. * Math.sin(2*Math.PI*i/10.);
		//			env.addOrganism(new SimpleCircleOrganism(env, 100.0, x, y));
		//		}
		env = new TestEnvironment(0L, false);
		((TestEnvironment) env).bindInput(mouse_buttons, mouse_move);
	}

	public void run(){
		// env.bindInput(mouse_buttons, mouse_move); // TestEnvironments only
		// opengl must be initialized in the same thread where it is used, so we need to create and
		//	add the RenderGL here.
		RenderGL renderpanel = new RenderGL(canvas, env, APPLET_WIDTH, APPLET_HEIGHT);
		renderpanel.bindInputs(direction_keys, mouse_move, mouse_buttons);

		try {
			Mouse.create();
			Keyboard.create();
		}
		catch (LWJGLException e) {
			e.printStackTrace();
		}

		long time = System.currentTimeMillis();

		// run simulation
		while(!Display.isCloseRequested()){
			long now = System.currentTimeMillis();
			double dt = ((double) (now - time)) / (double) TICK_MS;
			checkInput(renderpanel);
			renderpanel.moveCamera(dt);
			renderpanel.redraw();
			updateFPS(now);
			if(first_frame || !paused || (mouse_buttons[0] == 1 && !mouse_hold)){
				first_frame = false;
				mouse_hold = true;
				env.update(dt);
				if(env.getOrganismCount() == 0) break;
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

		// TESTING - 'M' for mutate (TestEnvironment and PointRodOrganisms ONLY
		//if(Keyboard.isKeyDown(Keyboard.KEY_M)) env.mutateTestGene();

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
		driver.run();
		window.dispose();
	}
}