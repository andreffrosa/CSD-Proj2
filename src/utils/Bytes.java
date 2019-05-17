package utils;

public class Bytes {

	public static long fromBytes(byte[] bytes) {
		long n = 0;
		
	    for (int i = 0; i < 8; i++) {
	        n <<= 8;
	        n |= (bytes[i] & 0xFF);
	    }
	    
	    return n;
	}
	
	public static byte[] toBytes(long n) {
		byte[] bytes = new byte[8];
	    
		for (int i = 7; i >= 0; i--) {
	        bytes[i] = (byte)(n & 0xFF);
	        n >>= 8;
	    }
		
	    return bytes;
	}
	
}
