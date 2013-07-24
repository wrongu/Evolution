package graphics.opengl;

import org.lwjgl.opengl.GL11;

public class Attribute{
		/** number of bytes to represent this attribute. Will be rounded up to the nearest multiple of 4
		 * (instsize) for my own convenience of dealing with (more common) ints and floats rather than bytes */
		public int bytesize, intsize;

		/** number of bytes into the vbo where this attribute appears */
		public int vbo_offset;

		/** the attribute's location in the program */
		public int location;

		
		 /** OpenGL type of the data. One of GL_FLOAT, GL_UNSIGNED_BYTE, etc		 */
		public int type;
		
		/** size of vector, type-independent. IE a vector of 4 bytes OR 4 ints has numel=4 */
		public int numel;

		/** normalized values will be restricted to the range [0,1]. This is useful for converting bytes [0, 255] to floats
		 * [0,1] automatically.*/
		public boolean normalized;

		/** the current value to use for this attribute for the next addVertex() calls.
		 * For convenience, keeping it as an int array rather than byte array */
		public int[] curValue;

		/** if this attribute requires setting a client state, store it here */
		public int clientStateId;

		/** determines whether or not this attribute should be used at all */
		public boolean active;

		/**
		 * create a new attribute
		 * @param size the number of bytes in this attribute
		 * @param required_state any client state that should be set for this attribute, such as GL_VERTEX_ARRAY or GL_COLOR_ARRAY
		 */
		public Attribute(int numel, int type, boolean normalize, int required_state){
			this.numel = numel;
			this.type = type;
			this.normalized = normalize;
			int sizeper = 0;
			switch(type){
			case GL11.GL_BYTE:
			case GL11.GL_UNSIGNED_BYTE:
				sizeper = 1;
				break;
			case GL11.GL_SHORT:
			case GL11.GL_UNSIGNED_SHORT:
				sizeper = 2;
				break;
			case GL11.GL_INT:
			case GL11.GL_UNSIGNED_INT:
			case GL11.GL_FLOAT:
				sizeper = 4;
				break;
			default:
				System.err.println("Unrecognized attribute type");
				break;
			}
			bytesize = numel * sizeper;
			// round up to next multiple of 4 bytes (for working with integers and floats)
			bytesize += ((0x04 - (bytesize & 0x03)) & 0x03);
			intsize = bytesize >> 2;
		curValue = new int[intsize];
		clientStateId = required_state;
		}

		public void writeToBuffer(int[] buffer, int offset){
			if(active){
				for(int i=0; i < intsize; i++){
					buffer[offset + i + (vbo_offset >> 2)] = curValue[i];
				}
			}
		}

		public void setValue(float ... data){
			if(data.length != intsize){
				System.err.println("Attribute size mismatch: my intsize is "+intsize+" but I received a write() call that contained "+data.length+" floats");
			} else{
				active = true;
				for(int i = 0; i < data.length; i++){
					curValue[i] = Float.floatToRawIntBits(data[i]);
				}
			}
		}

		public void setValue(int ... data){
			if(data.length != intsize)
				System.err.println("Attribute size mismatch: my intsize is "+intsize+" but I received a write() call that contained "+data.length+" ints");
			else{
				active = true;
				curValue = data;
			}
		}

		public void enableIfActive(){
			if(active && clientStateId != -1) GL11.glEnableClientState(clientStateId);
		}

		public void deactivate(){
			active = false;
			if(clientStateId != -1) GL11.glDisableClientState(clientStateId);
		}
}