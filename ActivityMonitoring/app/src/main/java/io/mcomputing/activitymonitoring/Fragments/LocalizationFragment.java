package io.mcomputing.activitymonitoring.Fragments;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.mcomputing.activitymonitoring.Adapters.MonitoringAdapter;
import io.mcomputing.activitymonitoring.R;
import io.mcomputing.activitymonitoring.UtilsManager;

public class LocalizationFragment extends Fragment implements SensorEventListener {

	public static final String TAG = "LocalizationFragment";
	private View view;
	private FragmentActivity activity;
	private SensorManager mSensorManager;
	private Sensor mAcceleroMeter;
	private final int MAX_SENSOR_VALUES = 16;
	private int countSensorValues = 0;
	private List<String> csvSensorValues = new ArrayList<>();

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		view = inflater.inflate(R.layout.localization_layout, null);
		activity = getActivity();

		return view;
	}

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		//mSensorManager.unregisterListener(this);
	}

	public static LocalizationFragment newInstance(){
		return new LocalizationFragment();
	}



	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		/*if(sensorType == Sensor.TYPE_ACCELEROMETER) {
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
					//getFeatureValue(file);
				}
				csvSensorValues.clear();
			}

		}*/

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
