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
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.mcomputing.activitymonitoring.Adapters.MonitoringAdapter;
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
	private final int MAX_SENSOR_VALUES = 64;
	private int countSensorValues = 0;
	private List<String> csvSensorValues = new ArrayList<>();
	private MonitoringAdapter adapter;
	private int trainActCount = 0;
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		view = inflater.inflate(R.layout.probability_layout, null);
		activity = getActivity();
		Bundle bundle = getArguments();
		if(bundle != null)
			trainActCount = bundle.getInt("act_count");
		setBackBtn();
		setTrainFeature();
		if(trainActCount > 0)
			generateItemList();
		else
			Toast.makeText(activity, getString(R.string.feature_extraction_txt), Toast.LENGTH_SHORT).show();
		return view;
	}

	private void setTrainFeature(){
		View run_feature = view.findViewById(R.id.run_feature);
		run_feature.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(trainActCount == 0) {
					final ProgressDialog dialog = ProgressDialog.show(activity, getString(R.string.extraction_title),
							getString(R.string.extraction_body), true);
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
								if(success)
									generateItemList();
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
				}else{
					Toast.makeText(activity, ProbabilityFragment.this.getString(R.string.features_extracted), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void setBackBtn(){
		View backBtn = view.findViewById(R.id.back_btn);
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.onBackPressed();
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
						initSensors();
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
			}
		}
	}

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		mSensorManager.unregisterListener(this);
	}

	private void getFeatureValue(File file){
		RequestParams params = new RequestParams();
		try {
			params.put("file", file);
			JSONAsyncTask.getFeature(params, new JsonHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
					// If the response is JSONObject instead of expected JSONArray
					Log.d("FEATURERESPONSE", "success:" + response);
					try { // TODO in Java
						String prob = response.getString("prob");
						String probArr = prob.split("Predict:")[1];
						adapter.setItemAtPos(0, probArr);
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
