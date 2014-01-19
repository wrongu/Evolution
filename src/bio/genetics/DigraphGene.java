package bio.genetics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import physics.PointMass;

import environment.Environment;
import structure.Organism;

/**
 * A gene for creating organisms by traversing a directed graph. Nodes in the graph correspond to joints in structure,
 * and edges correspond to limbs. Edges may be recursively followed to make substructures.
 * 
 * TODO javadoc
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
	
	public DigraphGene(){
		nodes = new HashMap<Integer, GraphNode>();
		edges = new ArrayList<GraphEdge>();
	}
	
	private static class GraphNode{
		// usual graph-y things
		private ArrayList<GraphEdge> edges_out;
		private int local_uid;
		// special values for graph traversal algorithm
		private PointMass last_instance;
		// genetic stuff that defines a joint
		public double mass;
		public double rest_low, rest_high;
		public boolean is_muscle;
		public double muscle_strength;
		
		public GraphNode(int id, double m, double rl, double rh, boolean muscle, double strength){
			this.local_uid = id;
			this.mass = m;
			this.rest_low = rl;
			this.rest_high = rh;
			this.is_muscle = muscle;
			this.muscle_strength = strength;
			this.edges_out = new ArrayList<GraphEdge>();
		}
		
		public void addEdge(GraphEdge e){
			assert(e.from.getId() == this.local_uid);
			this.edges_out.add(e);
		}
		
		public int getId(){
			return this.local_uid;
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
	
	@Override
	public ISexGene<Organism> mutate(double rate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Organism create(int posx, int posy, Environment e) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
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
			dest.writeDouble(node.rest_low);
			dest.writeDouble(node.rest_high);
			// next write muscle info: byte for T/F plus strength
			dest.writeBoolean(node.is_muscle);
			if(node.is_muscle) dest.writeDouble(node.muscle_strength);
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

	@Override
	public void deserialize(InputStream in) throws IOException {
		DataInputStream source = new DataInputStream(in);
		// parse nodes
		int id;
		boolean parsing_nodes = true;
		while(source.available() > 0){
			if(parsing_nodes){
				if((id = source.readInt()) == -2){
					parsing_nodes = false;
					continue;
				}
				double mass = source.readDouble();
				// read joint properties
				double rl = source.readDouble();
				double rh = source.readDouble();
				// read muscle properties
				boolean muscle = source.readBoolean();
				double strength = muscle ? source.readDouble() : 0.0;
				GraphNode n = new GraphNode(id, mass, rl, rh, muscle, strength);
				this.nodes.put(id, n);
			} else{
				// read IDs of nodes this edge connects to
				int from_id = source.readInt();
				int to_id = source.readInt();
				// read recursive depth
				int rec = source.readInt();
				boolean structural = source.readBoolean();
				if(!structural){
					// create edge
					GraphEdge e = new GraphEdge(this.nodes.get(from_id), this.nodes.get(to_id), rec);
					this.edges.add(e);
				} else{
					// read structure properties
					double rl = source.readDouble();
					double rh = source.readDouble();
					// read muscle properties
					boolean muscle = source.readBoolean();
					double strength = muscle ? source.readDouble() : 0.0;
					// create edge
					GraphEdge e = new GraphEdge(this.nodes.get(from_id), this.nodes.get(to_id), rec, rl, rh, muscle, strength);
					this.edges.add(e);
				}
			}
		}
	}
}
