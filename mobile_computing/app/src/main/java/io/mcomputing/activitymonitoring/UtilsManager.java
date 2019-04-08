package io.mcomputing.activitymonitoring;
import tensorflow as tf
import java.nio.ByteBuffer;
import java.security.SecureRandom;

public class UtilsManager {

	private static UtilsManager instance;

	public static UtilsManager getInstance(){
		if(instance == null)
			instance = new UtilsManager();
		return instance;
	}

	public static Long getGenericId(){
		SecureRandom sr = new SecureRandom();
		byte[] rndBytes = new byte[8];
		sr.nextBytes(rndBytes);
		return bytesToLong(rndBytes);
	}

	public static  long bytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(bytes);
		buffer.flip();//need flip
		return buffer.getLong();
	}

}
