package graphics.opengl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class VBOProgram {

	private static enum ProgramState {INIT, ACTIVE, INACTIVE};

	private HashMap<String, Attribute> attributes;
	// shortcut to the 3 most common attributes: vertices, color, and texture
	private Attribute colorAttribute, texAttribute, vertAttribute;
	private boolean colorSingleByte;
	private int byte_stride;

	private int[] rawBuffer;
	private int rawBufferIndex;
	private int vertexCount;
	private int vboId;

	private Program program;

	private int drawMode;
	private ProgramState myState;

	private ByteBuffer  byteBuffer;
	private IntBuffer   intBuffer;

	/**
	 * Create and return a new VBOProgram object that wraps the program p. It must specify some vertex attribute,
	 * so the name of the vertex attribute in the shader should be given in vertexName
	 * @param p the program which this VBOProgram wraps
	 * @param vertexName the name of the vertex attribute in the vertex shader
	 * @param is3D whether the vertex shader expects vec3 or vec2 vertex coordinates
	 * @return
	 */
	public static VBOProgram create(Program p, String vertexName, boolean is3D){
		VBOProgram ret = new VBOProgram(0x8000);
		ret.program = p;
		ret.addAttribute(vertexName, GL11.GL_FLOAT, (is3D ? 3 : 2), false, GL11.GL_VERTEX_ARRAY);	
		return ret;
	}

	private VBOProgram(int rawIntSize){
		rawBuffer = new int[rawIntSize];
		byteBuffer = BufferUtils.createByteBuffer(4 * rawIntSize);
		intBuffer = byteBuffer.asIntBuffer();
		vboId = GL15.glGenBuffers();
		myState = ProgramState.INIT;
		attributes = new HashMap<String, Attribute>();
	}


	/**
	 * Tell this VBOProgram object about another attribute of one of its shaders. This can only be done during the
	 * initialization phase of the program. That is, no further attributes can be added after initComplete() is called
	 * @param shaderName the name of the attribute
	 * @param glType the OpenGL type of the data. One of GL_FLOAT, GL_INT, etc
	 * @param numel the length of the vector of data. 1 for scalars.
	 * @param normalize whether to normalize the non-float values to [0,1] floats
	 * @param glState if this attribute requires setting a client state, put it here. IE if this attribute is texture
	 * coordinates, then you would set glState to GL11.GL_TEXTURE_COORD_ARRAY
	 */
	public void addAttribute(String shaderName, int glType, int numel, boolean normalize, int glState){
		if(myState == ProgramState.INIT){
			Attribute attr = new Attribute(numel, glType, normalize, glState);
			attributes.put(shaderName, attr);
			switch(glState){
			case GL11.GL_VERTEX_ARRAY:
				vertAttribute = attr;
				break;
			case GL11.GL_TEXTURE_COORD_ARRAY:
				texAttribute = attr;
				break;
			case GL11.GL_COLOR_ARRAY:
				colorAttribute = attr;
				colorSingleByte = (glType == GL11.GL_UNSIGNED_BYTE || glType == GL11.GL_BYTE);
				break;
			default:
				break;
			}
		}
		else
			System.err.println("Cannot add more attributes because VBOProgram is no longer in the INIT phase");
	}

	/**
	 * Tell this VBOProgram object about another attribute of one of its shaders. This can only be done during the
	 * initialization phase of the program. That is, no further attributes can be added after initComplete() is called.
	 * This attribute has no required "client state" such as GL11.GL_COLOR_ARRAY or GL11.GL_TEXTURE_ARRAY.
	 * @param shaderName the name of the attribute
	 * @param glType the OpenGL type of the data. One of GL_FLOAT, GL_INT, etc
	 * @param numel the length of the vector of data. 1 for scalars.
	 * @param normalize whether to normalize the non-float values to [0,1] floats
	 */
	public void addAttribute(String shaderName, int glType, int numel, boolean normalize){
		addAttribute(shaderName, glType, numel, normalize, -1);
	}

	/*
	 * FUNCTIONS TO SET ATTRIBUTE VALUES
	 */

	/**
	 * Set a single float or up to vec4 of float values to the given attribute.
	 * The attribute will retain this value for all addVertex() calls, until it is
	 * overwritten by another call to setAttributef()
	 * @param shaderName the name of the attribute in the shader
	 * @param values 1, 2, 3, or 4 floats
	 */
	public void setAttributef(String shaderName, float ... values){
		Attribute attr = attributes.get(shaderName);
		if(attr != null){
			attr.setValue(values);
		} else{
			System.err.println("No attribute '"+shaderName+"' has been added");
		}
	}

	/**
	 * Set a single int or up to 4 int values to the given attribute. In general, only use ints for special cases.
	 * @param shaderName the name of the attribute in the shader
	 * @param values 1, 2, 3, or 4 ints
	 */
	public void setAttributei(String shaderName, int ... values){
		Attribute attr = attributes.get(shaderName);
		if(attr != null){
			attr.setValue(values);
		} else{
			System.err.println("No attribute '"+shaderName+"' has been added");
		}
	}

	/*
	 * WRAPPERS FOR SETTING UNIFORMS
	 */

	/**
	 * Set a uniform value for the program. Can be a single float or up to 4 floats to
	 * specify a vec4
	 * @param shaderName name of the uniform in the shader
	 * @param values 1, 2, 3, or 4 floats which will become a scalar, vec2, vec3, or vec4 in the shader
	 */
	public void setUniformf(String shaderName, float ... values){
		program.begin();
		program.setUniform(shaderName, values);
		program.end();
	}

	/**
	 * Like setUniformf but for integers. In general, floats are preferred to ints.
	 * @see setUniformf
	 */
	public void setUniformi(String shaderName, int ... values){
		program.begin();
		program.setUniform(shaderName, values);
		program.end();
	}

	/**
	 * Like setUniformf but for a matrix
	 * @param shaderName name of the matrix in the shader
	 * @param flatMatrix a flipped buffer as a 1D representation of the matrix. must have size 4, 9, or 16
	 */
	public void setUniformMat(String shaderName, FloatBuffer flatMatrix){
		program.begin();
		program.setUniformMatrix(shaderName, flatMatrix);
		program.end();
	}

	/**
	 * after drawing is complete, this resets the buffers and sets the state to inactive, waiting
	 * for a new call to beginDrawing()
	 */
	private void reset(){
		if(myState == ProgramState.ACTIVE){
			rawBufferIndex = 0;
			byteBuffer.clear();
			myState = ProgramState.INACTIVE;
			vertexCount = 0;
		} else if (myState == ProgramState.INACTIVE){
			System.err.println("Cannot reset() vbo program; already inactive");
		} else{
			System.err.println("Cannot reset() program until initialization is done. Don't forget to call initComplete()");
		}
	}

	/**
	 * Call this function *once* after all attributes have been added to the program. After this, no more attributes
	 * may be added, because this function computes the stride and offset for this VBO with respect to the program's
	 * attributes.
	 */
	public void initComplete(){
		if(myState == ProgramState.INIT){
			int offset = 0;
			for(Map.Entry<String, Attribute> name_attr : attributes.entrySet()){
				String name = name_attr.getKey();
				Attribute attr = name_attr.getValue();

				attr.location = program.getAttributeLocation(name);
				attr.vbo_offset = offset;
				offset += attr.bytesize;
			}
			byte_stride = offset;
			myState = ProgramState.INACTIVE;
		}
		else
			System.err.println("In initComplete(): program has already been initialized");
	}

	/**
	 * Prepare this VBOProgram to receive addVertex() calls. After draw(), must make a new call to beginDrawing()
	 * @param gl_mode the drawing mode. For example, GL_QUADS, GL_LINES, GL_TRIANGLES, etc
	 */
	public void beginDrawing(int gl_mode){
		if(myState == ProgramState.INACTIVE){
			//			this.reset();
			drawMode = gl_mode;
			myState = ProgramState.ACTIVE;
		} else{
			System.err.println("already drawing!");
		}
	}

	/**
	 * add a 2D vertex. Other attributes will be applied according to their current value (see setAttribute)
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public void addVertex(float x, float y){
		if(myState != ProgramState.ACTIVE){
			System.err.println("Must call beginDrawing() before adding a vertex");
			return;
		}

		vertAttribute.setValue(x, y);

		for(Attribute attr : attributes.values())
			attr.writeToBuffer(rawBuffer, rawBufferIndex);

		rawBufferIndex += byte_stride >> 2;
		vertexCount++;
	}

	/**
	 * shorthand for setAttribute() followed by addVertex()
	 */
	public void addVertexWithAttribute(float x, float y, String attrName, float ... value){
		setAttributef(attrName, value);
		addVertex(x, y);
	}

	/**
	 * shorthand for setAttribute() followed by addVertex()
	 */
	public void addVertexWithAttribute(float x, float y, String attrName, int ... value){
		setAttributei(attrName, value);
		addVertex(x, y);
	}

	/**
	 * add a 3D vertex. Other attributes will be applied according to their current value (see setAttribute)
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 */
	public void addVertex(float x, float y, float z){
		if(myState != ProgramState.ACTIVE){
			System.err.println("Must call beginDrawing() before adding a vertex");
			return;
		}

		vertAttribute.setValue(x, y, z);

		for(Attribute attr : attributes.values())
			attr.writeToBuffer(rawBuffer, rawBufferIndex);

		rawBufferIndex += byte_stride << 2;
		vertexCount++;
		// TODO - if vertex count exceeds buffer size, draw it. problem: element arrays?
	}

	/**
	 * shorthand for setAttribute() followed by addVertex()
	 */
	public void addVertexWithAttribute(float x, float y, float z, String attrName, float ... value){
		setAttributef(attrName, value);
		addVertex(x, y, z);
	}

	/**
	 * shorthand for setAttribute() followed by addVertex()
	 */
	public void addVertexWithAttribute(float x, float y, float z, String attrName, int ... value){
		setAttributei(attrName, value);
		addVertex(x, y, z);
	}

	/**
	 * if an attribute has been specified with the state GL_COLOR_ARRAY, then it will be update by this call. If none
	 * has been set, this function does nothing.
	 */
	public void setColor(float r, float g, float b, float a){
		if(colorAttribute != null){
			if(!colorSingleByte) colorAttribute.setValue(r, g, b, a);
			else{
				int ri = 0xFF & ((int) (r * 255f));
				int gi = 0xFF & ((int) (g * 255f));
				int bi = 0xFF & ((int) (b * 255f));
				int ai = 0xFF & ((int) (a * 255f));

				int value = ri | (gi << 8) | (bi << 16) | (ai << 24);
				colorAttribute.setValue(value);
			}
		}
	}

	/**
	 * if an attribute has been specified with the state GL_TEXTURE_COORD_ARRAY, then it will be update by this call. If none
	 * has been set, this function does nothing.
	 */
	public void setTexCoord(float s, float t){
		if(texAttribute != null) texAttribute.setValue(s, t);
	}
	
	/**
	 * Equivalent to setTexCoord(s,t) followed by addVertex(x, y)
	 */
	public void addVertexWithTex(float x, float y, float s, float t){
		setTexCoord(s, t);
		addVertex(x, y);
	}
	
	/**
	 * Equivalent to setTexCoord(s,t) followed by addVertex(x, y, z)
	 */
	public void addVertexWithTex(float x, float y, float z, float s, float t){
		setTexCoord(s, t);
		addVertex(x, y, z);
	}

	/**
	 * Draw all buffered data that has accumulated since beginDrawing().
	 * Uses the drawing mode specified with beginDrawing(mode).
	 * Uses the context of the current program.
	 */
	public void draw(){
		if(myState == ProgramState.ACTIVE){
			if(rawBufferIndex == 0){
				System.err.println("no vertices have been added; drawing would result in a crash");
				return;
			}
			intBuffer.clear();
			intBuffer.put(rawBuffer, 0, rawBufferIndex);
			byteBuffer.rewind();
			byteBuffer.limit(rawBufferIndex * 4);

			program.begin();
			{
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, byteBuffer, GL15.GL_STREAM_DRAW);

				// turn on attributes and bind them to the proper locations in the vbo
				for(Attribute attr : attributes.values()){
					attr.enableIfActive();
					program.setVertexArrayAttrib(attr, byte_stride);
				}

				// draw
				GL11.glDrawArrays(drawMode, 0, vertexCount);

				// clean up attributes (i.e. deactivate them)
				for(Attribute attr : attributes.values()) attr.deactivate();
				program.unbindArrayAttribs();
			}
			program.end();

			reset();
		} else{
			System.err.println("Must call beginDrawing(mode) before adding vertices and drawing");
		}
	}

	public void destroy() {
		GL15.glDeleteBuffers(vboId);
	}

}
