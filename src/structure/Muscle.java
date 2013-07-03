package structure;

/**
 * Classes should extend this class if they can act out some motion. The best example is Links and Joints, which
 * can expand/contract and rotate, respectively
 * @author Richard
 *
 */
public abstract class Muscle {

	protected double rest_val;
	protected double current_val;	
	protected double strength;
	
	public Muscle(double rest_val){
		this.rest_val = rest_val;
		this.current_val = rest_val;
	}
	
	/**
	 * Enact whatever movement this class is capable of
	 * @param target the destination value. For Links, this is length, for joints it is the angle off of 'zero'.
	 * @param strength the force to apply. 0 means fully relaxed, 1 is full force. 
	 */
	public void setMovement(double target, double strength){
		this.strength = strength;
		this.current_val = target;
	}
	
	public void relax(){
		strength = 0.0;
	}
}
