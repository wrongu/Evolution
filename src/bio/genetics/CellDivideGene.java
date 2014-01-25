package bio.genetics;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import physics.PointMass;
import physics.Rod;

import environment.Environment;
import structure.Organism;
import utils.Levenshtein;
import utils.MathHelper;

public class CellDivideGene extends SexGene<Organism> {

	private static final char GOTO = 'G';
	private static final char LOOP = 'L';
	private static final char STEP = 'S';
	private static final char BRANCH = 'B';
	private static final char END = 'E';
	private static final char SEP = '.';
	private static final String META = "#";

	/**
	 * To avoid meta-information, genes specify their own max dist from other genes.
	 * Too low, and they can't reproduce. Too high, and their offspring suffer from deformities.
	 * Hopefully we'll find that the system settles on its own ideal notion of species-distance
	 */
	private int compatibledist;

	/**
	 * store the string representation of this gene, sans metadata. assume this is not updated as instructions are mutated.
	 * Use a call to reserialize() after all mutations are done.
	 */
	private String serial_gene;

	/**
	 * ArrayList containing all instructions. Assume they aren't ordered in any particular way, except that execution will
	 * begin with the 0th element
	 */
	private ArrayList<Instruction> instructionList;

	public CellDivideGene(String serialization){
		String[] split = serialization.split(META);
		if(split.length == 2){
			// parse first argument as compatibledist integer (hex format)
			compatibledist = MathHelper.readHexAsInt(split[0], 0);
			// save serialization
			serial_gene = split[1];
			// initialize instruction list, then begin populating it
			instructionList = new ArrayList<CellDivideGene.Instruction>();
			parseInstructions(split[1]);
		} else{
			System.err.println("CellDivideGene: serialization should have exactly 1 META ('"+META+"') flag, but had " + (split.length-1));
			System.err.println(serialization);
		}
	}

	/**
	 * parse the full serialized instructions, return the first one (root) 
	 * @param instructions the serialized, string representation of the instructions
	 */
	private void parseInstructions(String instructions) {
		int index = 0;
		Instruction curInst = null;
		while(index < instructions.length()){
			switch(instructions.charAt(index)){
			case GOTO:
				curInst = (new GotoInstruction()).read(instructions, index);
				break;
			case STEP:
				curInst = (new StepInstruction()).read(instructions, index);
				break;
			default:
				System.err.println("parse error at "+index+": "+instructions.charAt(index));
				System.err.println(instructions);
				for(int i=0; i<index; i++) System.err.print(" ");
				System.err.println("^");
				break;
			}
			instructionList.add(curInst);
			index += curInst.charSize() + 1;
		} 
	}
	
	@Override
	public SexGene<Organism> mutate(double rate) {

		// FIRST PASS: potentially alter +/- existing fields

		// SECOND PASS: potentially add new fields

		return null;
	}
	
	@Override
	public SexGene<Organism> cross(SexGene<Organism> mate, int minblock, int maxblock) throws IncompatibleParentsException {

		if(!isCompatible(mate)) throw new IncompatibleParentsException();
		CellDivideGene other = (CellDivideGene) mate;

		// TODO

		return null;
	}

	@Override
	public Organism create(double posx, double posy, Environment e) {

		/** PointMasses must have ids so that a loop instruction, for example, know which point to loop back to.*/
		HashMap<Integer, PointMass> pm_map = new HashMap<Integer, PointMass>();

		/** keep a list of rods for later creating the organism */
		ArrayList<Rod> rodList = new ArrayList<Rod>();

		/** As instructions are executed, there is a "current" pointmass from which new rods extend. this is that pointmass.*/
		PointMass pmPointer = new PointMass(PointMass.DEFAULT_MASS, new Rod[]{});

		/** initialize with a single default pointmass, id 0 */
		pm_map.put(0, pmPointer);

		/** stack of instructions to execute */
		Stack<Instruction> instr_stack = new Stack<CellDivideGene.Instruction>();

		for(int i = instructionList.size()-1; i >= 0; --i){
			instr_stack.push(instructionList.get(i));
		}

		while(!instr_stack.isEmpty()){
			Instruction curInst = instr_stack.pop();
			pmPointer = curInst.execute(instr_stack, pmPointer, pm_map, rodList);
		}

		ArrayList<PointMass> pmList = new ArrayList<PointMass>(pm_map.size());
		for(PointMass pm : pm_map.values())
			pmList.add(pm);
		
		// TODO pointmass locations
		Organism o = new Organism(e);
		o.addAllPointMasses(pmList);
		o.addAllRods(rodList);
		o.initStructure();
		
		return o;
	}

	/**
	 * Compatability check. first requires that each gene be a CellDivideGene. Second, the distance between the genes
	 * (computed as the Levenshtein distance between their serialized strings) must be less than the minimum allowable
	 * distance (compatibleDist) between the two parents
	 */
	@Override
	public boolean isCompatible(SexGene<Organism> mate) {
		if(!(mate instanceof CellDivideGene)) return false;

		CellDivideGene other = (CellDivideGene) mate;
		int genedist = Levenshtein.computeLevenshteinDistance(serial_gene, other.serial_gene);
		int maxdist = Math.min(compatibledist, other.compatibledist);

		return genedist < maxdist;
	}

	/**
	 * recompute the serialized string representation of this gene from the instructionList. includes metadata.
	 * @return
	 */
	public String reserialize(){
		StringBuilder builder = new StringBuilder();

		builder.append(compatibledist);
		builder.append(META);
		for(int i = 0; i < instructionList.size()-1; i++){
			instructionList.get(i).write(builder);
			builder.append(SEP);
		}
		instructionList.get(instructionList.size()-1).write(builder);
		return builder.toString();
	}


	/**
	 * Instructions include Recurse, Step, Loop, Branch, and End. Each may contain some amount of metadata
	 * which is encapsulated by these classes. Each of the instruction types has an associated class that
	 * extends the abstract Instruction class.
	 * @author Richard
	 *
	 */
	private abstract class Instruction{
		int id;
		int recurselimit;

		public Instruction(int i, int l){
			id = i;
			recurselimit = l;
		}

		public Instruction(){
			this(-1, -1);
		}

		public abstract Instruction read(String source, int index);

		/**
		 * Append the serialization of this instruction to the given stringbuilder
		 */
		public abstract void write(StringBuilder builder);

		/**
		 * Append the serialization of this instruction to the given stringbuilder
		 */
		void write(StringBuilder builder, char instr){
			builder.append(instr);
			MathHelper.writeIntAsHex(builder, id);
			MathHelper.writeIntAsHex(builder, recurselimit);
		}

		public abstract PointMass execute(Stack<Instruction> instrstack, PointMass pmCurrent, HashMap<Integer, PointMass> pm_map, ArrayList<Rod> rodList);

		/**
		 * count execution (i.e. decrement recurselimit)
		 */
		void execute(){
			recurselimit--;
		}

		/**
		 * @return the number of characters this instruction uses for serialization. 
		 */
		public int charSize(){
			// char name, 8-hex id, 8-hex recurselimit
			return 17;
		}

		@Override
		public boolean equals(Object i){
			if(i instanceof Instruction)
				return ((Instruction) i).id == id;
			else
				return false;
		}
	}

	/**
	 * Instruction that causes the current instruction pointer to jump to a different instruction (with target id)
	 * @author Richard
	 *
	 */
	private class GotoInstruction extends Instruction{

		private int target;

		public void write(StringBuilder builder) {
			super.write(builder, GOTO);
			MathHelper.writeIntAsHex(builder, target);
		}

		@Override
		public PointMass execute(Stack<Instruction> instrstack, PointMass pmCurrent, HashMap<Integer, PointMass> pm_map, ArrayList<Rod> rodList) {
			// set instrPointer to be the instruction with the given target id.
			// Note that if target is an invalid id, the search returns null and
			// 	processing halts.
			if(recurselimit > 0){
				for(Instruction instr : instructionList)
					if(instr.id == target){
						instrstack.push(instr);
						break;
					}
			}
			super.execute();
			return pmCurrent;
		}

		@Override
		public int charSize() {
			// 8-hex target id
			return super.charSize() + 8;
		}

		@Override
		public Instruction read(String source, int index) {
			if(source.charAt(index) != GOTO){
				System.err.println("read GOTO without GOTO character? index "+index);
				System.err.println(source);
			} else{
				this.id = MathHelper.readHexAsInt(source, index+1);
				this.recurselimit = MathHelper.readHexAsInt(source, index+9);
				this.target = MathHelper.readHexAsInt(source, index+17);
			}
			return this;
		}
	}

	/**
	 * Instruction that causes a rod to connect from the current point to another existing point,
	 * forming a loop (cycle) 
	 * @author Richard
	 */
	/*	private class LoopInstruction extends Instruction{

		private int point_target;
		private float minLength, maxLength;

		public void write(StringBuilder builder) {
			super.write(builder, LOOP);
			MathHelper.writeIntAsHex(builder, point_target);
			MathHelper.writeIntAsHex(builder, Float.floatToRawIntBits(minLength));
			MathHelper.writeIntAsHex(builder, Float.floatToRawIntBits(maxLength));
		}

		@Override
		public void execute(Stack<Instruction> instrstack, PointMass pmCurrent, HashMap<Integer, PointMass> pm_map, ArrayList<Rod> rodList) {
			if(recurselimit > 0){
				PointMass pmTarget = pm_map.get(point_target);
				if(pmTarget != null){
					Rod r = new Rod(minLength, maxLength, pmCurrent, pmTarget);
					rodList.add(r);
				}
			}
			super.execute();
		}

		@Override
		public int charSize() {
			// 8-hex target point id, 8-hex float minlength, 8-hex float maxlength
			return super.charSize() + 24;
		}

		@Override
		public void read(String source, int index) {
			if(source.charAt(index) != LOOP){
				System.err.println("read LOOP without LOOP character? index "+index);
				System.err.println(source);
			} else{
				this.id = MathHelper.readHexAsInt(source, index+1);
				this.recurselimit = MathHelper.readHexAsInt(source, index+9);
				this.point_target = MathHelper.readHexAsInt(source, index + 17);
				this.minLength = Float.intBitsToFloat(MathHelper.readHexAsInt(source, index + 25));
				this.maxLength = Float.intBitsToFloat(MathHelper.readHexAsInt(source, index + 33));
			}
		}
	}
	 */

	/**
	 * Instruction that creates a rod by "stepping" from the current pointmass. if the target already exists, it forms a loop.
	 * Otherwise, it creates a new pointmass (i.e. a dead-end segment)
	 * @author Richard
	 *
	 */
	private class StepInstruction extends Instruction{

		private int ptid;
		private float minLength, maxLength;

		@Override
		public Instruction read(String source, int index) {
			if(source.charAt(index) != STEP){
				System.err.println("read STEP without STEP character? index "+index);
				System.err.println(source);
			} else{
				this.id = MathHelper.readHexAsInt(source, index+1);
				this.recurselimit = MathHelper.readHexAsInt(source, index+9);
				this.ptid = MathHelper.readHexAsInt(source, index+17);
				this.minLength = Float.intBitsToFloat(MathHelper.readHexAsInt(source, index+25));
				this.maxLength = Float.intBitsToFloat(MathHelper.readHexAsInt(source, index+33));
			}
			return this;
		}

		@Override
		public void write(StringBuilder builder) {
			super.write(builder, STEP);
			MathHelper.writeIntAsHex(builder, ptid);
			MathHelper.writeIntAsHex(builder, Float.floatToRawIntBits(minLength));
			MathHelper.writeIntAsHex(builder, Float.floatToRawIntBits(maxLength));
		}

		@Override
		public PointMass execute(Stack<Instruction> instrstack, PointMass pmCurrent, HashMap<Integer, PointMass> pm_map, ArrayList<Rod> rodList) {
			if(recurselimit > 0){
				// get-or-create pointmass with id. (if it already exists, we are creating a loop. otherwise, it is a dead-end segment)
				PointMass targetpt = pm_map.get(ptid);
				if(targetpt == null){
					targetpt = new PointMass(PointMass.DEFAULT_MASS, new Rod[]{});
					pm_map.put(ptid, targetpt);
				}
				// unlikely, but we can't connect a point to itself
				if(pmCurrent != targetpt){
					Rod newrod = new Rod(minLength, maxLength, pmCurrent, targetpt);
					rodList.add(newrod);
					return targetpt;
				}
			}
			super.execute();
			return pmCurrent;
		}
		
		@Override
		public int charSize(){
			// 8-hex id, 8-hex each min and max length
			return super.charSize() + 24;
		}

	}

	@Override
	public void serialize(OutputStream dest) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void deserialize(InputStream reader) {
		// TODO Auto-generated method stub
		
	}

	/*
	private class BranchInstruction extends Instruction{

		@Override
		public void read(String source, int index) {
			if(source.charAt(index) != BRANCH){
				System.err.println("read BRANCH without BRANCH character? index "+index);
				System.err.println(source);
			} else{
				this.id = MathHelper.readHexAsInt(source, index+1);
				this.recurselimit = MathHelper.readHexAsInt(source, index+9);
			}
		}

		@Override
		public void write(StringBuilder builder) {
			super.write(builder, BRANCH);
		}

		@Override
		public void execute(Stack<Instruction> instrstack, PointMass pmCurrent,
				HashMap<Integer, PointMass> pm_map, ArrayList<Rod> rodList) {
			// TODO Auto-generated method stub

		}


	}
	 */

	/*
	private class EndInstruction extends Instruction{

		@Override
		public void read(String source, int index) {
			if(source.charAt(index) != GOTO){
				System.err.println("read GOTO without GOTO character? index "+index);
				System.err.println(source);
			} else{
				this.id = MathHelper.readHexAsInt(source, index+1);
				this.recurselimit = MathHelper.readHexAsInt(source, index+9);
				// TODO
			}
		}

		@Override
		public void write(StringBuilder builder) {
			// TODO Auto-generated method stub

		}

		@Override
		public void execute(Stack<Instruction> instrstack, PointMass pmCurrent,
				HashMap<Integer, PointMass> pm_map, ArrayList<Rod> rodList) {
			// TODO Auto-generated method stub

		}

	}
	 */
}
