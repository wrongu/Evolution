package bio.genetics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import environment.Environment;

import structure.Organism;

public class ByteArrayGene implements ISexGene<Organism> {
	
	public static Random rand = new Random();
	
	// data
	private byte[] data;
	// write and read indices
	private int wi, ri, size;
	
	public ByteArrayGene(){
		wi = ri = size = 0;
		data = new byte[1024];
	}
	
	private ByteArrayGene(ByteArrayGene copy){
		data = copy.data.clone();
		this.wi = copy.wi;
		this.ri = copy.ri;
	}
	
	public void write(byte b){
		if(wi == data.length){
			byte[] temp = new byte[data.length*2];
			System.arraycopy(data, 0, temp, 0, data.length);
			data = temp;
		}
		data[wi++] = b;
		size = wi > size ? wi : size;
	}
	
	public void write(int i){
		for(int k = 0; k < 4; k++){
			write((byte) (i & 0xFF));
			i >>= 8;
		}
	}
	
	public void write(long l){
		for(int i = 0; i < 8; i++){
			write((byte) (l & 0xFF));
			l >>= 8;
		}
	}
	
	public void write(float f){
		write(Float.floatToIntBits(f));
	}
	
	public void write(boolean b){
		write(b ? (byte) 1 : (byte) 0);
	}
	
	public void write(double d){
		write(Double.doubleToLongBits(d));
	}
	
	public byte readByte(){
		if(ri < size)
			return data[ri++];
		else
			return (byte) 0;
	}
	
	public int readInt(){
		int ret = 0;
		for(int i = 0; i < 4; i++){
			ret |= readByte() << 24;
			ret >>= 8;
		}
		return ret;
	}
	
	public long readLong(){
		long ret = 0;
		for(int i = 0; i < 8; i++){
			ret |= readByte() << 56;
			ret >>= 8;
		}
		return ret;
	}
	
	public boolean readBool(){
		return (readByte() == 0 ? false : true);
	}
	
	public float readFloat(){
		return Float.intBitsToFloat(readInt());
	}
	
	public double readDouble(){
		return Double.longBitsToDouble(readLong());
	}
	
	public ByteArrayGene mutate(double rate){
		ByteArrayGene g = new ByteArrayGene(this);
		for(int i = 0; i < size; i++){
			if(Math.random() < rate){
				g.data[i] = (byte) (rand.nextInt() & 0xFF);
			}
		}
		return g;
	}
	
	public ByteArrayGene duplicate(double rate){
		ByteArrayGene g = new ByteArrayGene();
		g.wi = 0;
		ri = 0;
		while(ri < size){
			byte val = readByte();
			g.write(val);
			if(Math.random() < rate)
				g.write(val);
		}
		return g;
	}
	
	public void writeToFile(File f) throws IOException {
		ri = 0;
		OutputStream os = new FileOutputStream(f);
		os.write(size);
		os.write(this.data, 0, size);
		os.flush();
		os.close();
	}
	
	public static ByteArrayGene readFromFile(File f) throws IOException {
		InputStream is = new FileInputStream(f);
		ByteArrayGene g = new ByteArrayGene();
		int streamlen = 0;
		for(int i = 0; i < 4; i++){
			streamlen += is.read();
			streamlen <<= 8;
		}
		g.data = new byte[nextPowerOf2(streamlen)];
		g.wi = is.read(g.data);
		is.close();
		return g;
	}
	
	private static int nextPowerOf2(int v){
		v--;
		v |= v >> 1;
		v |= v >> 2;
		v |= v >> 4;
		v |= v >> 8;
		v |= v >> 16;
		v++;
		return v;
	}

	public ISexGene<Organism> cross(ISexGene<Organism> other, int minblock, int maxblock) {
		ByteArrayGene byteother = (ByteArrayGene) other;
		ByteArrayGene g = new ByteArrayGene(this);
		g.data = new byte[Math.max(this.data.length, byteother.data.length)];
		int i = 0;
		int minl = Math.min(size, byteother.size);
		while(i < minl){
			int step = (int) Math.floor(Math.random() * (maxblock - minblock)) + minblock;
			if(i + step > minl) step = minl - i;
			ByteArrayGene from = Math.random() < 0.5 ? this : byteother;
			System.arraycopy(from.data, i, g.data, i, step);
			i += step;
		}
		// copy remainder
		if(size > minl)
			System.arraycopy(this.data, minl, g.data, minl, size - minl);
		else if(byteother.size > minl)
			System.arraycopy(byteother.data, minl, g.data, minl, byteother.size - minl);
		return g;
	}

	public Organism create(double posx, double posy, Environment e) {
		// TODO create organism from this gene
		return null;
	}

	public boolean isCompatible(ISexGene<Organism> other) {
		// TODO Auto-generated method stub
		return false;
	}

	public void serialize(OutputStream dest) {
		// TODO Auto-generated method stub
		
	}

	public void deserialize(InputStream reader) {
		// TODO Auto-generated method stub
		
	}
}
