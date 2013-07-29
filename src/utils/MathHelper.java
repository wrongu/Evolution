package utils;

import javax.xml.bind.DatatypeConverter;

public class MathHelper {

	public static byte[] hexToBytes(String hex){
		return DatatypeConverter.parseHexBinary(hex);
	}
	
	public static void writeIntAsHex(StringBuilder b, int i){
		for(int j = 0; j < 8; j++){
			b.append(intToHexChar((i >> 28) & 0xF));
			i <<= 4;
		}
	}
	
	public static int readHexAsInt(String s, int index){
		int ret = 0;
		for(int i = index; i < index+8; ++i){
			ret <<= 4;
			ret |= hexCharToInt(s.charAt(i));
		}
		return ret;
	}
	
	public static char intToHexChar(int i){
		int masked = (i & 0xF); 
		return (char) (masked < 10 ? masked + '0' : masked - 10 + 'A'); 
	}
	
	public static int hexCharToInt(char c){
		if(c >= 'A' && c <= 'F') return  c - 'A' + 10;
		else return c - '0';
	}
	
	public static float readHexAsFloat(String s, int index){
		return Float.intBitsToFloat(readHexAsInt(s, index));
	}
	
	public static void writeFloatAsHex(StringBuilder builder, float f){
		writeIntAsHex(builder, Float.floatToRawIntBits(f));
	}
	
	// TESTING/DEBUGGING
	public static void main(String[] args){
		for(int i = 0; i < 0xFF+1; i++){
			StringBuilder b = new StringBuilder();
			writeIntAsHex(b, i);
			System.out.println(i + "\t0x" + b.toString() + "\t" + readHexAsInt(b.toString(), 0));
		}
	}
}
