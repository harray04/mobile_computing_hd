package io.mcomputing.activitymonitoring.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FileDownloadTask;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.mcomputing.activitymonitoring.Activities.MainActivity;
import io.mcomputing.activitymonitoring.Adapters.MonitoringAdapter;
import io.mcomputing.activitymonitoring.Helpers.DeleteCallback;
import io.mcomputing.activitymonitoring.Helpers.FileManager;
import io.mcomputing.activitymonitoring.Helpers.TestRecord;
import io.mcomputing.activitymonitoring.Helpers.knn;
import io.mcomputing.activitymonitoring.JSONAsyncTask;
import io.mcomputing.activitymonitoring.Models.ActivityListModel;
import io.mcomputing.activitymonitoring.R;
import io.mcomputing.activitymonitoring.UtilsManager;

public class ProbabilityFragment extends Fragment implements SensorEventListener {
	public static final String TAG = "ProbabilityFragment";
	private View view;
	private FragmentActivity activity;
	private SensorManager mSensorManager;
	private Sensor mAcceleroMeter;
	private final int MAX_SENSOR_VALUES = 16;
	private int countSensorValues = 0;
	private List<String> csvSensorValues = new ArrayList<>();
	private MonitoringAdapter adapter;
	private int trainActCount = 0;
	private boolean ready = false;
	private View mapBtn;
	private boolean isRegistered = false;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		view = inflater.inflate(R.layout.probability_layout, null);
		activity = getActivity();
		ready = false;
		Bundle bundle = getArguments();
		if(bundle != null)
			trainActCount = bundle.getInt("act_count");
		setBtns();
		setTrainFeature();
		initList();
		getAccuracy();
		//UtilsManager.downloadFile(activity);
		return view;
	}

	private void getAccuracy(){
		if(ready){
			final ProgressDialog dialog = ProgressDialog.show(activity, getString(R.string.train_knn),
					getString(R.string.train_knn_body), true);
			Task<FileDownloadTask.TaskSnapshot> task1 = UtilsManager.downloadFile(activity, "train_features.csv");
			Task<FileDownloadTask.TaskSnapshot> task2 = UtilsManager.downloadFile(activity, "test_features.csv");

			Tasks.whenAll(task1, task2).addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					final String filePath1 = activity.getFilesDir().getPath() + "/Final/train_features.csv";
					final String filePath2 = activity.getFilesDir().getPath() + "/Final/test_features.csv";

					final TextView acc_tv = view.findViewById(R.id.accuracy_tv);
					acc_tv.setVisibility(View.VISIBLE);
					new Thread(new Runnable() {
						@Override
						public void run() {
							final double acc = knn.knn(filePath1, filePath2, 12);
							activity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									acc_tv.setText("Accuracy: " + String.valueOf(acc) + '%');
									initSensors();
									dialog.dismiss();
									mapBtn.setVisibility(View.VISIBLE);
								}
							});

						}
					}).start();


				}
			});
					//  updateDb(timestamp,localFile.toString(),positio
		}
	}

	private void initList(){
		JSONAsyncTask.getExtracted(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// If the response is JSONObject instead of expected JSONArray
				Log.d("TRAINFEATURE", "success:" + response);
				try {
					boolean success = false;
					if (response != null) {
						success = response.getBoolean("is_ready");
					}
					ready = success;
					if (success) {
						generateItemList();
					}

					getAccuracy();

				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable,
								  JSONObject errorResponse) {
				// Pull out the first event on the public timeline
				Toast.makeText(activity, errorResponse.toString(), Toast.LENGTH_LONG).show();
				Log.d("TRAINFEATURE", "error:" + errorResponse);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				Log.d("TRAINFEATURE", "error1:" + responseString);
				Toast.makeText(activity, responseString, Toast.LENGTH_LONG).show();
			}
		});

	}

	private void setTrainFeature(){
		View run_feature = view.findViewById(R.id.run_feature);
		run_feature.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final ProgressDialog dialog = ProgressDialog.show(activity, getString(R.string.extraction_title),
						getString(R.string.extraction_body), true);
				JSONAsyncTask.getExtracted(new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
						// If the response is JSONObject instead of expected JSONArray
						Log.d("TRAINFEATURE", "success:" + response);
						try {
							boolean success = false;
							if (response != null) {
								success = response.getBoolean("is_ready");
							}
							ready = success;
							if (success) {
								dialog.dismiss();
								DeleteFeaturesDialog deleteFeaturesDialog = DeleteFeaturesDialog.newInstance(getString(R.string.delete_trained_features), getString(R.string.delete_trained_features_body), new DeleteCallback() {
									@Override
									public void onDeleted() {
										Toast.makeText(activity,
												ProbabilityFragment.this.getString(R.string.delete_features_success),
												Toast.LENGTH_SHORT).show();
										mSensorManager.unregisterListener(ProbabilityFragment.this);
										mapBtn.setVisibility(View.GONE);
										final TextView acc_tv = view.findViewById(R.id.accuracy_tv);
										acc_tv.setVisibility(View.GONE);
									}

									@Override
									public void onDeletedFailed() {
										Toast.makeText(activity,
												ProbabilityFragment.this.getString(R.string.delete_features_failed),
												Toast.LENGTH_SHORT).show();
									}
								});
								deleteFeaturesDialog.show(((MainActivity) activity).getSupportFragmentManager(), DeleteFeaturesDialog.TAG);
							}else {
								startFeatureExtraction(dialog);
							}

						} catch (JSONException e) {
							dialog.dismiss();
							e.printStackTrace();
						}

					}

					@Override
					public void onFailure(int statusCode, Header[] headers, Throwable throwable,
										  JSONObject errorResponse) {
						// Pull out the first event on the public timeline
						Toast.makeText(activity, errorResponse.toString(), Toast.LENGTH_LONG).show();
						Log.d("TRAINFEATURE", "error:" + errorResponse);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
						Log.d("TRAINFEATURE", "error1:" + responseString);
						Toast.makeText(activity, responseString, Toast.LENGTH_LONG).show();
					}
				});

			}

		});
	}

	private void startFeatureExtraction(final ProgressDialog dialog){
		JSONAsyncTask.trainFeature(String.valueOf(trainActCount), new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// If the response is JSONObject instead of expected JSONArray
				Log.d("TRAINFEATURE", "success:" + response);
				dialog.dismiss();
				if(response != null)
					Toast.makeText(activity, response.toString(), Toast.LENGTH_LONG).show();
				try {
					boolean success = false;
					if (response != null) {
						success = response.getBoolean("success");
					}
					ready = success;
					if(success)
						generateItemList();
					getAccuracy();
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable,
								  JSONObject errorResponse) {
				// Pull out the first event on the public timeline
				dialog.dismiss();
				Toast.makeText(activity, errorResponse.toString(), Toast.LENGTH_LONG).show();
				Log.d("TRAINFEATURE", "error:" + errorResponse);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				Log.d("TRAINFEATURE", "error1:" + responseString);
				dialog.dismiss();
				Toast.makeText(activity, responseString, Toast.LENGTH_LONG).show();
			}
		});
	}

	private void setBtns(){
		View backBtn = view.findViewById(R.id.back_btn);
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onBackPressed();
			}
		});

		mapBtn = view.findViewById(R.id.map_btn);
		mapBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((MainActivity)activity).loadFragment(R.id.content_main, LocalizationFragment.newInstance(), LocalizationFragment.TAG);
			}
		});
	}

	public static ProbabilityFragment newInstance(){
		return new ProbabilityFragment();
	}

	private void generateItemList() {
		JSONAsyncTask.getActivities(new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
				// If the response is JSONObject instead of expected JSONArray
				Log.d("ACTIVITPROB", "success:" + response);
				List<ActivityListModel> activityModels = new ArrayList<>();
				try {
					if(response.length() > 0) {
						for (int i = 0; i < response.length(); i++) {
							JSONObject object = response.getJSONObject(i);
							String actName = object.getString("name") + ':';
							ActivityListModel actMod =
									new ActivityListModel(UtilsManager.getGenericId(),
											actName, "");
							activityModels.add(actMod);
						}
						adapter = new MonitoringAdapter(activity, activityModels);
						ListView itemsListView  = (ListView) view.findViewById(R.id.probability_listview);
						itemsListView.setAdapter(adapter);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}


			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable,
								  JSONObject errorResponse){
				// Pull out the first event on the public timeline
				Log.d("ACTIVITPROB", "error:" + errorResponse);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				Log.d("ACTIVITPROB", "error1:" + responseString);
			}
		});
	}

	private void initSensors() {
		if (activity != null) {
			mSensorManager =
					(SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
			if (mSensorManager != null) {
				mAcceleroMeter =
						mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			}


			if (mAcceleroMeter != null) {
				mSensorManager.registerListener(this, mAcceleroMeter,
						SensorManager.SENSOR_DELAY_NORMAL);
				isRegistered = true;
			}
		}
	}

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		if(isRegistered) {
			isRegistered = false;
			mSensorManager.unregisterListener(this);
		}
	}

	private void getFeatureValue(File file){
		RequestParams params = new RequestParams();
		try {
			params.put("file", file);
			JSONAsyncTask.getFeature(params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
					// If the response is JSONObject instead of expected JSONArray


					try { // TODO in Java
						String filePath = activity.getFilesDir().getPath() + "/Final/train_features.csv";
						File train_features = new File(filePath);
						if(train_features.exists()){
							String data = response.getString("data");
							TestRecord testRecord = FileManager.createNewTestRecord(data);
							HashMap<Integer, Double> knn_prob = knn.knn_predict(filePath, testRecord, 12);
							if(knn_prob != null) {
								adapter.setItems(knn_prob);
							}
						}


						//adapter.setItemAtPos(0, probArr);
						//Toast.makeText(activity, prob, Toast.LENGTH_SHORT).show();
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}

				@Override
				public void onFailure(int statusCode, Header[] headers, Throwable throwable,
									  JSONObject errorResponse){
					// Pull out the first event on the public timeline
					Log.d("FEATURERESPONSE", "error:" + errorResponse);
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
					Log.d("FEATURERESPONSE", "error1:" + responseString);
				}
			});
		} catch(FileNotFoundException e) {

		}


	}



	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		if(sensorType == Sensor.TYPE_ACCELEROMETER) {
			String x_axis = String.valueOf(event.values[0]);
			String y_axis = String.valueOf(event.values[1]);
			String z_axis = String.valueOf(event.values[2]);

			countSensorValues++;
			csvSensorValues.add(x_axis + ',' + y_axis + ',' + z_axis);
			if(countSensorValues % MAX_SENSOR_VALUES == 0){
				countSensorValues = 0;
				File file = UtilsManager.writeFile(activity, "file.csv", csvSensorValues, true);
				Log.d("FEATURERESPONSE", "size:" + csvSensorValues.size());
				if(file != null){
					getFeatureValue(file);
				}
				csvSensorValues.clear();
			}

		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
