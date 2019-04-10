package io.mcomputing.activitymonitoring;
import android.graphics.Color;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;

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

	public static int getRandomColor(){
		Random rnd = new Random();
		int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
		return color;
	}


	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

}
