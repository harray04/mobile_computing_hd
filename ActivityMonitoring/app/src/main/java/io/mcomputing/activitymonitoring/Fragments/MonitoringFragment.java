package io.mcomputing.activitymonitoring.Fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.storage.UploadTask;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import io.mcomputing.activitymonitoring.Activities.MainActivity;
import io.mcomputing.activitymonitoring.Adapters.SensorDataAdapter;
import io.mcomputing.activitymonitoring.JSONAsyncTask;
import io.mcomputing.activitymonitoring.Models.ActivityModel;
import io.mcomputing.activitymonitoring.Models.ChartModel;
import io.mcomputing.activitymonitoring.R;
import io.mcomputing.activitymonitoring.UtilsManager;

public class MonitoringFragment extends Fragment implements SensorEventListener {
	public static final String TAG = "MonitoringFragment";
	public static final int MAX_VISIBLE_VALUE_COUNT = 3;
	private static final int MAX_ACTIVITY_COUNT = 4;
	private static final int MAX_MONITORING_TIME = 5; // in minutes
	private LineChart multiLineChart;
	private SensorManager mSensorManager;
	//private Sensor mSensorProximity;
	private Sensor mAcceleroMeter;
	//private Sensor mGyroScope;
	private Activity activity;
	private float thresHold = 0;
	private int activityCount = 0;
	private View view;
	private RecyclerView gridView;
	private float timeStamp = 0;
	private HashMap<String, Long> sensorDataIds = new HashMap<>();
	private SensorDataAdapter sensorAdapter;
	private List<ChartModel> chartModels = new ArrayList<>();
	private long initialTimeStamp = 0;
	private List<ActivityModel> activityModels = new ArrayList<>();
	private TextView indicator;
	private EditText nameEditText;
	private int monitoringState = 0;
	private TextView activityBtn;
	private List<String> activityNames = new ArrayList<>();

	private Object lock = new Object();
	private FloatingActionButton nextBtn;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);

		view = inflater.inflate(R.layout.monitoring_layout, null);
		multiLineChart = view.findViewById(R.id.am_multiline_chart);
		nextBtn = (FloatingActionButton)view.findViewById(R.id.show_probability);
		activity = getActivity();
		//initSensors();
		checkHasProb();
		setActivityActions();
		initChart();
		setTimeOutHandler();
		return view;
	}



	private void checkHasProb(){
		nextBtn.hide();
		nextBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ProbabilityFragment fragment = ProbabilityFragment.newInstance();
				Bundle bundle = new Bundle();
				bundle.putInt("act_count", activityCount);
				fragment.setArguments(bundle);
				((MainActivity)activity).loadFragment(R.id.content_main,
						fragment, ProbabilityFragment.TAG);
			}
		});
		JSONAsyncTask.hasProb(new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// If the response is JSONObject instead of expected JSONArray
				Log.d("RESPONSEBODY", "success:" + response);

				try {
					boolean success = (boolean) response.getBoolean("is_ready");
					if(success) {
						int count = response.getInt("act_count");
						activityCount = count;
						nameEditText.setEnabled(false);
						activityBtn.setText(R.string.reset);
						monitoringState = 2;
						indicator.setText(String.valueOf(activityCount - 1));
						nextBtn.show();
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable,
								  JSONObject errorResponse){
				// Pull out the first event on the public timeline
				Log.d("RESPONSEBODY", "error:" + errorResponse);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
				Log.d("RESPONSEBODY", "error1:" + responseString);
			}
		});
	}

	private void setActivityActions(){
		indicator = (TextView)view.findViewById(R.id.activity_indicator);
		nameEditText = (EditText)view.findViewById(R.id.activity_name);
		activityBtn = (TextView)view.findViewById(R.id.activity_btn);
		activityBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finiteStateMachine();
			}
		});

	}

	private void finiteStateMachine(){
		String name = nameEditText.getText().toString();
		String activityBtnName = getString(R.string.error);
		boolean editTextEnabled = true;
		switch (monitoringState){
			case 0:
				if(activityCount < 4 && !name.isEmpty()) {
					activityNames.add(name);
					editTextEnabled = false;
					activityBtnName = getString(R.string.stop);
					activityBtn.setText(R.string.stop);
					resetChart();
					activityCount++;
					monitoringState = 1;
				}

				if(name.isEmpty()){
					Toast.makeText(activity, "You have to insert a Name.", Toast.LENGTH_SHORT).show();
				}

				break;
			case 1:
				resetChart();
				activityCount--;
				editTextEnabled = true;
				activityBtnName = getString(R.string.start);
				activityNames.remove(activityNames.size() - 1);
				monitoringState = 0;
				break;
			case 2:
				activityCount = 0;
				editTextEnabled = true;
				activityBtnName = getString(R.string.start);
				activityNames.clear();
				monitoringState = 0;
				break;
		}

		nameEditText.setEnabled(editTextEnabled);
		activityBtn.setText(activityBtnName);
		indicator.setText(String.valueOf(activityCount));
	}

	private void setTimeOutHandler(){
		final Handler handler = new Handler();
		handler.postDelayed(
				new Runnable() {
					public void run() {
						//Log.d("POSTDELAYES", "hier");
						updateChart();
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								setTimeOutHandler();
							}
						}, 500);
					}
				},
				500);
	}

	private int getChartModelPosition(Long id){
		int counter = 0;
		for(ChartModel chartModel: chartModels){
			if(chartModel.getSensorId().equals(id))
				return counter;
			counter++;
		}
		return -1;
	}

	private List<ChartModel> initSensors(){
		if(activity != null) {
			mSensorManager =
					(SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
			if(mSensorManager != null) {
				/*mSensorProximity =
						mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);*/
				mAcceleroMeter =
						mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				//mGyroScope =
				//		mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
				gridView = (RecyclerView) view.findViewById(R.id.sensor_list);
				GridLayoutManager gridLayoutManager = new GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false);

				gridView.setLayoutManager(gridLayoutManager);
				List<Sensor> sensorList =
						mSensorManager.getSensorList(Sensor.TYPE_ALL);

				for (Sensor currentSensor : sensorList) {

					String name = currentSensor.getName();
					if(currentSensor.getType() == Sensor.TYPE_ACCELEROMETER){
						//ActivityModel accelerometerX = new ActivityModel(id,
						//		currentSensor.getName() + ':', "", color, Color.LTGRAY)
						Log.d("HALLOHILAWDA", "hier");
						addSensor(name + "_x");
						addSensor(name + "_y");
						addSensor(name + "_z");
					}/*else if(currentSensor.getType() == Sensor.TYPE_GYROSCOPE) {
						addSensor(name + "_x");
						addSensor(name + "_y");
						addSensor(name + "_z");
					}else{
						//addSensor(name);
					}*/
				}

				sensorAdapter = new SensorDataAdapter(activity, activityModels) {
					@Override
					public void onClickSensorData(Long id, boolean isVisible, int position) {
						int visibleCount = getVisibleValueCount();
						if(visibleCount < MAX_VISIBLE_VALUE_COUNT || !isVisible) {
							sensorAdapter.updateOnClick(position, isVisible);
							int chartPosition = getChartModelPosition(id);
							if (chartPosition != -1) {
								ChartModel chartModel = chartModels.get(chartPosition);
								chartModel.setIsVisible(isVisible);
								chartModels.set(chartPosition, chartModel);
							}
							updateChart();
						}


					}
				};
				gridView.setAdapter(sensorAdapter);

				if (mAcceleroMeter != null) {
					mSensorManager.registerListener(this, mAcceleroMeter,
							SensorManager.SENSOR_DELAY_NORMAL);
				}
				return chartModels;
			}
		}
		return new ArrayList<>();
	}

	private void addSensor(String name){
		//long id = UtilsManager.getGenericId();
		int color = UtilsManager.getRandomColor();
		ActivityModel activityModel = new ActivityModel(name, "", color, Color.LTGRAY);
		activityModels.add(activityModel);
		ChartModel chartModel = new ChartModel(activityModel.getId(), name, new ArrayList<Entry>(), color, color);
		chartModels.add(chartModel);
		sensorDataIds.put(name, activityModel.getId());
	}

	private int getVisibleValueCount(){
		int count = 0;
		for(ChartModel chartModel: chartModels){
			if(chartModel.isVisible())
				count++;
		}
		return count;
	}

	private void updateChart(){
		List<ILineDataSet> lineDataSets = new ArrayList<>();
		for (ChartModel chartModel: chartModels) {
			LineDataSet lineDataSet = new LineDataSet(chartModel.getValue(), chartModel.getLabel());
			lineDataSet.setDrawCircles(false);
			lineDataSet.setColor(chartModel.getColor());
			lineDataSet.setCircleColor(chartModel.getCircleColor());
			lineDataSet.setVisible(chartModel.isVisible());
			lineDataSets.add(lineDataSet);
		}
		LineData lineData = new LineData(lineDataSets);
		multiLineChart.setData(lineData);
		multiLineChart.invalidate();
	}

	private String numberToTime(float value){
		int absValue = (int)Math.floor(value);

		int seconds = (int)((value - absValue) * 60.f);

		float hoursAndMinutes = absValue / 60.f;
		int hours = (int)Math.floor(hoursAndMinutes);


		int minutes = (int)((hoursAndMinutes - hours) * 60.f);

		String time;
		if(hours != 0){
			time = String.valueOf(hours) + 'h' + String.valueOf(minutes) + 'm' + String.valueOf(seconds) + 's';
		}else if(minutes != 0){
			time = String.valueOf(minutes) + 'm' + String.valueOf(seconds) + 's';
		}else{
			time = String.valueOf(seconds) + 's';
		}

		return time;

	}

	private void initChart(){
		List<ILineDataSet> lineDataSets = new ArrayList<>();
		chartModels = initSensors();
		for (ChartModel chartModel: chartModels) {
			LineDataSet lineDataSet = new LineDataSet(chartModel.getValue(), chartModel.getLabel());
			lineDataSet.setColor(chartModel.getColor());
			lineDataSet.setCircleColor(chartModel.getCircleColor());
			lineDataSet.setVisible(chartModel.isVisible());
			lineDataSets.add(lineDataSet);
		}

		//final String[] x_axis = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
		LineData lineData = new LineData(lineDataSets);
		XAxis xAxis = multiLineChart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setDrawGridLines(false);
		xAxis.setAxisLineWidth(2);
		xAxis.setTextSize(12f);
		xAxis.setTextColor(Color.BLACK);
		xAxis.setAxisMinimum(0);
		//xAxis.setLabelCount(x_axis.length,true);
		xAxis.setGranularity(1f);
		xAxis.setAxisMaximum(MAX_MONITORING_TIME);
		xAxis.setLabelCount(7, true);
		xAxis.setValueFormatter(new ValueFormatter() {
			@Override
			public String getFormattedValue(float value) {
				return numberToTime(value);
			}
		});
		/*xAxis.setValueFormatter(new ValueFormatter() {
			@Override
			public String getFormattedValue(float value) {
				return x_axis[(int)value];
			}
		});*/
		multiLineChart.getDescription().setEnabled(false);
		multiLineChart.getLegend().setEnabled(false);
		multiLineChart.setMaxVisibleValueCount(MAX_VISIBLE_VALUE_COUNT);
		multiLineChart.setViewPortOffsets(60, 0, 50, 100);
		multiLineChart.setData(lineData);
	}

	public static MonitoringFragment newInstance(){
		return new MonitoringFragment();
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d("SENSORMANAGER", "register");
		//resetChart();
		/*if (mSensorProximity != null) {
			mSensorManager.registerListener(this, mSensorProximity,
					SensorManager.SENSOR_DELAY_NORMAL);
		}*/

		/*if (mGyroScope != null) {
			mSensorManager.registerListener(this, mGyroScope,
					SensorManager.SENSOR_DELAY_NORMAL);
		}*/
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView(){
		super.onDestroyView();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		timeStamp = (System.currentTimeMillis() - initialTimeStamp)/(1000.f * 60.f); // in minutes


			resetAndSaveChart();
			switch (sensorType) {
				// Event came from the light sensor.
				/*case Sensor.TYPE_PROXIMITY:
					float currentValue = event.values[0];
					Sensor sensorProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
					setSensorForChart(sensorProximity.getName(), currentValue, true);
					break;*/
				case Sensor.TYPE_ACCELEROMETER:
					float x_axis = event.values[0];
					float y_axis = event.values[1];
					float z_axis = event.values[2];


					Sensor sensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
					String accelName = sensorAccelerometer.getName();
					setSensorForChart(accelName + "_x", x_axis, false);
					setSensorForChart(accelName + "_y", y_axis, false);
					setSensorForChart(accelName + "_z", z_axis, false);
					//" z:" + z_axis);
					break;
				/*case Sensor.TYPE_GYROSCOPE:
					float yGyro = event.values[0];
					float zGyro = event.values[1];
					float xGyro = event.values[2];

					Sensor sensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
					String gyroName = sensorGyroscope.getName();
					setSensorForChart(gyroName + "_y", yGyro, false);
					setSensorForChart(gyroName + "_x", xGyro, false);
					setSensorForChart(gyroName + "_z", zGyro, false);

					break;*/
				default:
					// do nothing
			}

			//updateChart();
	}

	private void setSensorForChart(String name, float value, boolean isInstant){
		Long id = sensorDataIds.get(name);
		//Log.d("SHOWMEID", "setSensorForChart id:" + id + " name:" + name);
		sensorAdapter.updateItem(id, "value:" + UtilsManager.round(value, 1)
				, isInstant);

		if(chartModels != null){
			int position = getChartModelPosition(id);
			//Log.d("CHARTPOSITION", "p:" + position);
			if(position != -1){
				ChartModel chartModel = chartModels.get(position);
				chartModel.updateValue(new Entry(timeStamp, value));
				chartModels.set(position, chartModel);
			}
		}
	}

	private ChartModel getChartModelByName(String name){
		Long id = sensorDataIds.get(name);
		for(ChartModel chartModel: chartModels){
			if(chartModel.getSensorId().equals(id)){
				return chartModel;
			}
		}
		return null;
	}

	private List<String> createCSVStringList(){
		List<String> stringList = new ArrayList<>();
		String accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).getName();
		ChartModel accelX = getChartModelByName(accelerometer + "_x");
		ChartModel accelY = getChartModelByName(accelerometer + "_y");
		ChartModel accelZ = getChartModelByName(accelerometer + "_z");

		if(accelX != null && accelY != null && accelZ != null) {
			for (int i = 0; i < accelX.getValue().size(); i++){
				String x = String.valueOf(accelX.getValue().get(i).getY());
				String y = String.valueOf(accelY.getValue().get(i).getY());
				String z = String.valueOf(accelZ.getValue().get(i).getY());
				stringList.add(x + ',' + y + ',' + z);
			}
		}

		return stringList;
	}

	private void resetChart(){
		initialTimeStamp = System.currentTimeMillis();
		timeStamp = 0;
		for(ChartModel chartModel: chartModels){
			Log.d("SIZESIZE", "s:" + chartModel.getValue().size());
			chartModel.resetValue();
		}
	}


	private void postActivities(){
		JSONArray jsonArray = new JSONArray();
		int counter = 0;
		try {
			for (String actName : activityNames) {
				JSONObject jsonObject = new JSONObject();

				jsonObject.put("name", actName);

				jsonArray.put(counter, jsonObject);
				counter++;
			}

			JSONAsyncTask.setActivities(activity, jsonArray,new AsyncHttpResponseHandler(){
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					Log.d("ACTIVITYPOST", "success");
				}
				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)  {
					Log.d("ACTIVITYPOST", "error");
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void resetAndSaveChart(){
		if(timeStamp >= MAX_MONITORING_TIME ){
			if(monitoringState == 1) {
				if (activityCount < MAX_ACTIVITY_COUNT) {
					UtilsManager.writeFile(activity, (activityCount - 1) + ".csv", createCSVStringList(), false);
					activityBtn.setText(R.string.start);
					nameEditText.setEnabled(true);
					monitoringState = 0;
				} else if (activityCount == MAX_ACTIVITY_COUNT) {
					UtilsManager.writeFile(activity, (activityCount - 1) + ".csv", createCSVStringList(), false);
					nameEditText.setEnabled(false);
					postActivities();
					activityBtn.setText(R.string.reset);
					monitoringState = 2;
				}
			}

			resetChart();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
