package structure;

public class Joint extends Muscle {
	
	private Link link1, link2;
	
	public Joint(double rest_angle, Link l1, Link l2){
		super(rest_angle);
		link1 = l1;
		link2 = l2;
	}
	
	public void setMovement(double target, double strength){
		super.setMovement(target, strength);
		
	}
}
