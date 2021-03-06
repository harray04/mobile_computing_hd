package io.mcomputing.activitymonitoring;
import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.List;
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


	public static File writeFile(Context context, String title, List<String> lines, boolean isCreateFile) {
		DataOutputStream fOutStream = null;
		String newLine = System.getProperty("line.separator");
		String path = context.getFilesDir().getPath() + '/' + title;
		Log.d("UPLOADFILE", "before PATH: "+path);

		File newFile = new File(path);
		Log.d("RESPONSE", "1");

		if(!newFile.exists()) {
			Log.d("RESPONSE", "2");
			try {
				boolean createFile = newFile.createNewFile();
				if(!createFile)
					return null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {

			fOutStream = new DataOutputStream(new FileOutputStream(path));
			for (String line : lines) {
				fOutStream.writeBytes(line);
				fOutStream.writeBytes(newLine);
				fOutStream.flush();
			}
			Log.d("RESPONSE", "4");
			if(!isCreateFile)
				uploadFile(context, path, title);
			else
				return newFile;

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				if (fOutStream != null) {
					fOutStream.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return null;
	}

	private static void uploadFile(Context context, String path, String title){
		FirebaseStorage storage = FirebaseStorage.getInstance(context.getString(R.string.firebase_storage));
		StorageReference storageRef = storage.getReference();
		StorageReference activityRef = storageRef.child(title);

		DataInputStream dataInputStream = null;
		try {
			dataInputStream = new DataInputStream(new FileInputStream(path));
			UploadTask uploadTask = activityRef.putStream(dataInputStream);

			uploadTask.addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception exception) {
					// Handle unsuccessful uploads
					Log.d("UPLOADFILE", "error:" + exception.getMessage());

				}
			}).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
					Log.d("UPLOADFILE", "success:" + taskSnapshot.toString());
					// taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
					// ...
				}
			});
		} catch (FileNotFoundException e) {
			Log.d("UPLOADFILE", "ada");
			e.printStackTrace();
		}

	}

	public static long stringToLong(String name){
		long h = 1125899906842597L; // prime
		int len = name.length();

		for (int i = 0; i < len; i++) {
			h = 31*h + name.charAt(i);
		}
		return h;
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

	public static Task<FileDownloadTask.TaskSnapshot> downloadFile(Context context, String fileName) {
		FirebaseStorage storage = FirebaseStorage.getInstance();
		StorageReference storageRef = storage.getReferenceFromUrl(context.getString(R.string.firebase_storage));
		StorageReference  islandRef = storageRef.child(fileName);
		String path = context.getFilesDir().getPath();
		File rootPath = new File(path, "Final");
		if(!rootPath.exists()) {
			rootPath.mkdirs();
		}

		final File localFile = new File(rootPath,fileName);

		Task<FileDownloadTask.TaskSnapshot> task = islandRef.getFile(localFile);
		task.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
			@Override
			public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
				Log.d("SUCCESSTASK", "hier:" + taskSnapshot.getTotalByteCount());
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Log.d("SUCCESSTASK", "fail:" + e.getMessage());
			}
		});
		return task;
	}

}
