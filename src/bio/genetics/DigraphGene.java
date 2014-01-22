package bio.genetics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Vector2d;

import physics.Joint;
import physics.PointMass;
import physics.Rod;

import environment.Environment;
import structure.Muscle;
import structure.Organism;

import static applet.Util.clamp_radians;

/**
 * A gene for creating organisms by traversing a directed graph. Nodes in the graph correspond to joints in structure,
 * and edges correspond to limbs. Edges may be recursively followed to make substructures.
 * 
 * TODO javadoc
 * TODO tests (serialization, mutation, f(g) properties)
 */
public class DigraphGene implements IGene<Organism> {
	
	// members of the digraphgene
	private GraphNode root;
	// note that if we could guarantee that this graph is connected, then only the root would be needed
	// to track the full structure (by traversing it). But here I assume that there may be 'latent' nodes
	// or edges with the gene that will be mutated with everything else. Having a list of nodes and edges
	// also makes serialization and deserialization easier
	private HashMap<Integer, GraphNode> nodes;
	private ArrayList<GraphEdge> edges;
	
	// shared reference vector for 'horizontal'
	private static final Vector2d HORIZONTAL = new Vector2d(1.0, 0.0);
	
	// lists used during construction
	private ArrayList<PointMass> all_points;
	private ArrayList<Rod> all_rods;
	private ArrayList<Joint> all_joints;
	private ArrayList<Muscle> all_muscles;
	
	public DigraphGene(){
		nodes = new HashMap<Integer, GraphNode>();
		edges = new ArrayList<GraphEdge>();
	}
	
	private static class GraphNode{
		// usual graph-y things
		private ArrayList<GraphEdge> edges_out;
		private int local_uid;
		// special values for graph traversal algorithm
		public PointMass last_instance;
		public Rod last_incident_rod;
		// genetic stuff that defines a joint
		public double mass;
		public boolean is_joint;
		public double rest_low, rest_high;
		public boolean is_muscle;
		public double muscle_strength;
		
		/**
		 * Constructor for unjointed nodes
		 * @param id unique id
		 * @param m mass of corresponding PointMasses
		 */
		public GraphNode(int id, double m){
			this(id, m, 0.0, 0.0, 0.0);
			this.is_joint = false;
			this.is_muscle = false;
		}
		
		/**
		 * Constructor for jointed but non-muscle nodes
		 * @param id unique id
		 * @param m mass of corresponding PointMasses
		 * @param rl low angle limit
		 * @param rh high angle limit
		 */
		public GraphNode(int id, double m, double rl, double rh){
			this(id, m, rl, rh, 0.0);
			this.is_muscle = false;
		}
		
		/**
		 * Constructor for jointed, muscle-driven nodes
		 * @param id unique id
		 * @param m mass of corresponding PointMasses
		 * @param rl low angle limit
		 * @param rh high angle limit
		 * @param strength muscle strength
		 */
		public GraphNode(int id, double m, double rl, double rh, double strength){
			this.local_uid = id;
			this.mass = m;
			this.rest_low = rl;
			this.rest_high = rh;
			this.muscle_strength = strength;
			this.is_joint = true;
			this.is_muscle = true;
			this.edges_out = new ArrayList<GraphEdge>();
		}
		
		public void addEdge(GraphEdge e){
			assert(e.from.getId() == this.local_uid);
			this.edges_out.add(e);
		}
		
		public int getId(){
			return this.local_uid;
		}
		
		public PointMass createPointMass(Rod incident){
			this.last_instance = new PointMass(this.mass);
			this.last_incident_rod = incident;
			return this.last_instance;
		}
	}
	
	private static class GraphEdge{
		// graph-y things
		private GraphNode from, to;
		public int max_recurse;
		public int current_recurse;
		// special values for graph traversal algorithm
		private boolean is_structure;
		// TODO scale (or other affine transform) for recursive steps
		// genetic/organism stuff for making rods
		public double rest_low, rest_high;
		public boolean is_muscle;
		public double muscle_strength;
		
		public GraphEdge(GraphNode from, GraphNode to, int rec, double rl, double rh, boolean muscle, double strength){
			this.from = from;
			this.to = to;
			this.max_recurse = rec;
			this.is_structure = true;
			this.rest_low = rl;
			this.rest_high = rh;
			this.is_muscle = muscle;
			this.muscle_strength = strength;
			this.recursionReset();
			this.from.addEdge(this);
		}
		
		public GraphEdge(GraphNode from, GraphNode to, int rec){
			this(from, to, rec, 0.0, 0.0, false, 0.0);
			this.is_structure = false;
		}
		
		public void recursionReset(){
			this.current_recurse = 0;
		}
		
		public boolean isStructure(){
			return this.is_structure;
		}
	}
	
	public DigraphGene mutate(double rate) {
		// (maybe) alter existing graph elements
		for(GraphNode n: this.nodes.values()){
			
		}
		for(GraphEdge e: this.edges){
			
		}
		
		// (maybe) add new graph elements
		
		
		return null;
	}

	public Organism create(double posx, double posy, Environment e) {
		// ensure that all edges are reset in terms of recursion depth
		for(GraphEdge ed : this.edges) ed.recursionReset();
		// initialize arrays
		this.all_points = new ArrayList<PointMass>();
		this.all_rods = new ArrayList<Rod>();
		this.all_joints = new ArrayList<Joint>();
		this.all_muscles = new ArrayList<Muscle>();
		// start creating structure from the root
		PointMass init_pm = this.root.createPointMass(null);
		init_pm.initPosition(posx, posy);
		// crazy traversal algorithm
		traverse_helper(this.root);
		Organism o = new Organism(e);
		o.addAllPointMasses(all_points);
		o.addAllRods(all_rods);
		o.addAllJoints(all_joints);
		o.addAllMuscles(all_muscles);
		return o;
	}
	
	private void instantiateGraphEdge(GraphEdge e, GraphNode parent){
		// "goto" only works if destination already exists.
		// (that is, "goto" edges will instantiate objects iff the destination node is yet unvisited)
		if(e.isStructure() || e.to.last_instance == null){
			// instantiate

			// create a rod for this edge with it's first point equal to
			// where we are coming from
			Rod r = new Rod(e.rest_low, e.rest_high);
			this.all_rods.add(r);
			r.addPoint(parent.last_instance);
			
			// if 'parent' is a joint, set it's parameters here
			if(parent.is_joint && parent.last_incident_rod != null){
				Joint j = new Joint(
						clamp_radians(parent.rest_low), clamp_radians(parent.rest_high),
						parent.last_instance, parent.last_incident_rod, r);
				this.all_joints.add(j);
				// if parent is a driven joint, add the muscle
				Muscle m = new Muscle(j, parent.muscle_strength);
				this.all_muscles.add(m);
			}
			
			// create muscle for this rod if specified
			if(e.is_muscle){
				Muscle m = new Muscle(r, e.muscle_strength);
				this.all_muscles.add(m);
			}
			
			// create point-mass at destination node
			PointMass next = e.to.createPointMass(r);
			this.all_points.add(next);
			// decide where it goes using some trig
			double l = (r.getRestValue1() + r.getRestValue2()) / 2.0;
			// angle of the rod based on joint and previous rod
			double angle = parent.last_incident_rod.asVector().angle(HORIZONTAL);
			// add in mean joint angle
			if(parent.is_joint)
				angle += (clamp_radians(root.rest_low) + clamp_radians(root.rest_high)) / 2.0;
			// compute x and y positions based on parent's position
			double x = parent.last_instance.getX() + l * Math.cos(angle);
			double y = parent.last_instance.getY() + l * Math.sin(angle);
			// set the position
			next.initPosition(x, y);
		}
	}
	
	private void traverse_helper(GraphNode root){
		for(GraphEdge e : root.edges_out){
			if(e.current_recurse <= e.max_recurse){
				// mark another step of dfs traversal
				e.current_recurse += 1;
				// create structures
				this.instantiateGraphEdge(e, root);
				// recurse (DFS)
				// note that if no structure was created, this is basically where "goto" happens
				this.traverse_helper(e.to);
				// when traversal is done (i.e. when the above line is finished),
				// set recursive depth back to 0
				e.recursionReset();
			}
		}
	}

	public void serialize(OutputStream s) throws IOException {
		DataOutputStream dest = new DataOutputStream(s);
		// NOTE that local_uid is never mutated but only used for convenience when
		// serializing and deserializing.
		//
		// write a bunch of vertices
		for(GraphNode node : this.nodes.values()){
			// write node id first
			dest.writeInt(node.getId());
			// next write three doubles for mass and rest angles
			dest.writeDouble(node.mass);
			dest.writeBoolean(node.is_joint);
			if(node.is_joint){
				dest.writeDouble(node.rest_low);
				dest.writeDouble(node.rest_high);
				// next write muscle info: byte for T/F plus strength
				dest.writeBoolean(node.is_muscle);
				if(node.is_muscle) dest.writeDouble(node.muscle_strength);
			}
		}
		// separator to denote end of nodes (avoiding -1 since that marks end-of-file)
		dest.writeInt(-2);
		// write a bunch of edges
		for(GraphEdge edge : this.edges){
			// write graph porperties
			dest.writeInt(edge.from.getId());
			dest.writeInt(edge.to.getId());
			dest.writeInt(edge.max_recurse);
			dest.writeBoolean(edge.isStructure());
			if(edge.isStructure()){
				// write rod structure properties
				dest.writeDouble(edge.rest_low);
				dest.writeDouble(edge.rest_high);
				// write muscle properties
				dest.writeBoolean(edge.is_muscle);
				if(edge.is_muscle) dest.writeDouble(edge.muscle_strength);
			}
		}
	}

	public void deserialize(InputStream in) throws IOException {
		DataInputStream source = new DataInputStream(in);
		// parse nodes
		int id;
		GraphNode n;
		GraphEdge e;
		boolean parsing_nodes = true;
		while(source.available() > 0){
			if(parsing_nodes){
				if((id = source.readInt()) == -2){
					parsing_nodes = false;
					continue;
				}
				double mass = source.readDouble();
				// read joint properties
				boolean joint = source.readBoolean();
				if(joint){
					double rl = source.readDouble();
					double rh = source.readDouble();
					// read muscle properties
					boolean muscle = source.readBoolean();
					if(muscle){
						double strength = source.readDouble();
						n = new GraphNode(id, mass, rl, rh, strength);
					} else{
						n = new GraphNode(id, mass, rl, rh);
					}
				} else{
					n = new GraphNode(id, mass);
				}
				this.nodes.put(id, n);
				// set root to the first node
				if(this.root == null) this.root = n;
			} else{
				// read IDs of nodes this edge connects to
				int from_id = source.readInt();
				int to_id = source.readInt();
				// read recursive depth
				int rec = source.readInt();
				boolean structural = source.readBoolean();
				if(!structural){
					// create edge
					e = new GraphEdge(this.nodes.get(from_id), this.nodes.get(to_id), rec);
					this.edges.add(e);
				} else{
					// read structure properties
					double rl = source.readDouble();
					double rh = source.readDouble();
					// read muscle properties
					boolean muscle = source.readBoolean();
					double strength = muscle ? source.readDouble() : 0.0;
					// create edge
					e = new GraphEdge(this.nodes.get(from_id), this.nodes.get(to_id), rec, rl, rh, muscle, strength);
					this.edges.add(e);
				}
			}
		}
	}
}
