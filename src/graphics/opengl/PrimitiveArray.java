package graphics.opengl;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
//import org.lwjgl.opengl.GL30;

public class PrimitiveArray {

	private static final int X_OFFSET = 0;
	private static final int Y_OFFSET = 1;
	private static final int S_OFFSET = 2;
	private static final int T_OFFSET = 3;
	private static final int COLOR_OFFSET = 4;
	/** The total number of 4-byte attributes for each vertex */
	public static final int INT_STRIDE = 5;
	/** The total number of bytes to specify all data for a single vertex */
	public static final int BYTE_STRIDE = INT_STRIDE << 2;

	private int[] rawBuffer;
	private int bufferSize;
	private int rawBufferIndex;
	private int vertexCount;
	private int vboId;
	//	private int vaoId;

	private int vAttrib, tAttrib, cAttrib;
	private Program program;

	private int drawMode;
	private boolean isDrawing;

	private ByteBuffer  byteBuffer;
	private ShortBuffer shortBuffer;
	private IntBuffer   intBuffer;
	private FloatBuffer floatBuffer;

	private float curTexS, curTexT;
	private int curColorBytes;

	private boolean useTex;
	// always use color (default to white)

	public static PrimitiveArray create(int vbos){
		return new PrimitiveArray(0x8000, vbos);
	}

	private PrimitiveArray(int rawIntSize, int n_vbos){
		// vaoId = GL30.glGenVertexArrays();

		rawBuffer = new int[rawIntSize];
		bufferSize = rawIntSize;
		byteBuffer = BufferUtils.createByteBuffer(4 * rawIntSize);
		shortBuffer = byteBuffer.asShortBuffer();
		intBuffer = byteBuffer.asIntBuffer();
		floatBuffer = byteBuffer.asFloatBuffer();
		vboId = GL15.glGenBuffers();
	}

	private void reset(){
		rawBufferIndex = 0;
		byteBuffer.clear();
		isDrawing = false;
		vertexCount = 0;
		curColorBytes = 1; // reset to white
		//		GL30.glBindVertexArray(0);
	}

	public void beginDrawing(int gl_mode){
		if(!isDrawing){
			this.reset();
			//			GL30.glBindVertexArray(vaoId);
			drawMode = gl_mode;
			isDrawing = true;
			useTex = false;
		} else{
			System.err.println("already drawing!");
		}
	}

	public void setProgram(Program p, String v2DAttribName, String tAttribName, String cAttribName){
		program = p;
		vAttrib = p.getAttribute(v2DAttribName);
		tAttrib = p.getAttribute(tAttribName);
		cAttrib = p.getAttribute(cAttribName);
	}

	public void addTexVertex(float x, float y, float s, float t){
		setTexCoords(s, t);
		addVertex(x, y);
	}

	public void addVertex(float x, float y){

		if(useTex){
			rawBuffer[rawBufferIndex + S_OFFSET] = Float.floatToRawIntBits(curTexS);
			rawBuffer[rawBufferIndex + T_OFFSET] = Float.floatToRawIntBits(curTexT);
		}

		rawBuffer[rawBufferIndex + COLOR_OFFSET] = curColorBytes;


		rawBuffer[rawBufferIndex + X_OFFSET] = Float.floatToRawIntBits(x);
		rawBuffer[rawBufferIndex + Y_OFFSET] = Float.floatToRawIntBits(y);

		rawBufferIndex += INT_STRIDE;
		vertexCount++;

		// TODO - if vertex count exceeds buffer size, draw it. problem: element arrays?
	}

	public void setTexCoords(float s, float t){
		curTexS = s;
		curTexT = t;
		useTex = true;
	}

	public void setColor(float r, float g, float b){
		r = r > 1f ? 1f : (r < 0f ? 0f : r);
		g = g > 1f ? 1f : (g < 0f ? 0f : g);
		b = b > 1f ? 1f : (b < 0f ? 0f : b);
		setColor(
				(int) (r * 255f),
				(int) (g * 255f),
				(int) (b * 255f));
	}

	public void setColor(int r, int g, int b){
		curColorBytes = (b << 16) | (g << 8) | r;
	}


	public void draw(){
		if(!isDrawing){
			System.err.println("not currently drawing");
			return;
		}

		intBuffer.clear();
		intBuffer.put(rawBuffer, 0, rawBufferIndex);
		byteBuffer.position(0);
		byteBuffer.limit(rawBufferIndex * 4);

		if(program != null) program.begin();

		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, byteBuffer, GL15.GL_STREAM_DRAW);

		if(useTex){
			if(program != null && tAttrib >= 0)
				program.setVertexArrayAttrib(tAttrib, 2, GL11.GL_FLOAT, BYTE_STRIDE, S_OFFSET << 2);
			else
				GL11.glTexCoordPointer(2, GL11.GL_FLOAT, BYTE_STRIDE, S_OFFSET << 2);
			GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		}

		if(program != null && cAttrib >= 0)
			program.setVertexArrayAttribNormalize(cAttrib, 4, GL11.GL_UNSIGNED_BYTE, BYTE_STRIDE, COLOR_OFFSET << 2);
		else
			GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, BYTE_STRIDE, COLOR_OFFSET << 2);
		GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

		if(program != null && vAttrib >= 0)
			program.setVertexArrayAttrib(vAttrib, 2, GL11.GL_FLOAT, BYTE_STRIDE, X_OFFSET << 2);
		else
			GL11.glVertexPointer(2, GL11.GL_FLOAT, BYTE_STRIDE, X_OFFSET << 2);
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);

		GL11.glDrawArrays(drawMode, 0, vertexCount);

		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
		if(useTex) 		GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

		if(program != null) program.end();

		reset();
	}

	public void destroy() {
		GL15.glDeleteBuffers(vboId);
		//		GL30.glDeleteVertexArrays(vaoId);
	}
}
