package io.mcomputing.activitymonitoring.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.mcomputing.activitymonitoring.Adapters.MonitoringAdapter;
import io.mcomputing.activitymonitoring.Adapters.SensorDataAdapter;
import io.mcomputing.activitymonitoring.Models.ActivityListModel;
import io.mcomputing.activitymonitoring.Models.ActivityModel;
import io.mcomputing.activitymonitoring.Models.ChartModel;
import io.mcomputing.activitymonitoring.R;
import io.mcomputing.activitymonitoring.UtilsManager;

public class MonitoringFragment extends Fragment implements SensorEventListener {
	public static final String TAG = "MonitoringFragment";
	public static final int MAX_VISIBLE_VALUE_COUNT = 3;
	private LineChart multiLineChart;
	private SensorManager mSensorManager;
	private Sensor mSensorProximity;
	private Sensor mAcceleroMeter;
	private Sensor mGyroScope;
	private Activity activity;
	private View view;
	private RecyclerView gridView;
	private float timeStamp = 0;
	private HashMap<String, Long> sensorDataIds = new HashMap<>();
	private SensorDataAdapter sensorAdapter;
	private List<ChartModel> chartModels;
	private long initialTimeStamp = 0;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);

		view = inflater.inflate(R.layout.monitoring_layout, null);
		multiLineChart = view.findViewById(R.id.am_multiline_chart);
		activity = getActivity();
		setListView();
		initSensors();

		initChart();
		return view;
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
				mSensorProximity =
						mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
				mAcceleroMeter =
						mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				mGyroScope =
						mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
				gridView = (RecyclerView) view.findViewById(R.id.sensor_list);
				GridLayoutManager gridLayoutManager = new GridLayoutManager(activity, 2, GridLayoutManager.HORIZONTAL, false);

				gridView.setLayoutManager(gridLayoutManager);
				List<Sensor> sensorList =
						mSensorManager.getSensorList(Sensor.TYPE_ALL);


				final List<ActivityModel> monitoringAdapters = new ArrayList<>();
				final List<ChartModel> chartModels = new ArrayList<>();
				for (Sensor currentSensor : sensorList) {
					long id = UtilsManager.getGenericId();

					int color = UtilsManager.getRandomColor();
					ActivityModel activityModel = new ActivityModel(id,
							currentSensor.getName() + ':', "", color, Color.LTGRAY);
					monitoringAdapters.add(activityModel);
					ChartModel chartModel = new ChartModel(id, currentSensor.getName(), new ArrayList<Entry>(), color, color);
					chartModels.add(chartModel);
					sensorDataIds.put(currentSensor.getName(), id);
				}

				sensorAdapter = new SensorDataAdapter(activity, monitoringAdapters) {
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
				return chartModels;
			}
		}
		return new ArrayList<>();
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
		//new Handler(new Runnable())
		List<ILineDataSet> lineDataSets = new ArrayList<>();
		for (ChartModel chartModel: chartModels) {
			LineDataSet lineDataSet = new LineDataSet(chartModel.getValue(), chartModel.getLabel());
			lineDataSet.setColor(chartModel.getColor());
			lineDataSet.setCircleColor(chartModel.getCircleColor());
			lineDataSet.setVisible(chartModel.isVisible());
			lineDataSets.add(lineDataSet);
		}
		LineData lineData = new LineData(lineDataSets);
		multiLineChart.setData(lineData);
		multiLineChart.invalidate();
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

		final String[] x_axis = new String[] {"0", "1", "2", "3", "4", "5"};
		LineData lineData = new LineData(lineDataSets);
		XAxis xAxis = multiLineChart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setDrawGridLines(false);
		xAxis.setAxisLineWidth(2);
		xAxis.setTextSize(12f);
		xAxis.setTextColor(Color.BLACK);
		xAxis.setLabelCount(x_axis.length,true);
		xAxis.setGranularity(1f);
		xAxis.setAxisMaximum(10);
		/*xAxis.setValueFormatter(new ValueFormatter() {
			@Override
			public String getFormattedValue(float value) {
				return x_axis[(int)value];
			}
		});*/

		multiLineChart.getLegend().setEnabled(false);
		multiLineChart.setMaxVisibleValueCount(MAX_VISIBLE_VALUE_COUNT);
		multiLineChart.setViewPortOffsets(60, 0, 50, 100);
		multiLineChart.setData(lineData);
	}

	private void setListView(){
		List<ActivityListModel> monitoringModels = generateItemList();
		MonitoringAdapter adapter = new MonitoringAdapter(activity, monitoringModels);
		ListView itemsListView  = (ListView) view.findViewById(R.id.monitoring_listview);
		itemsListView.setAdapter(adapter);
	}

	private List<ActivityListModel> generateItemList() {
		List<ActivityListModel> activityModels = new ArrayList<>();
		ActivityListModel running = new ActivityListModel(UtilsManager.getGenericId(), "Running:", "");
		ActivityListModel walking = new ActivityListModel(UtilsManager.getGenericId(), "Walking:", "");
		ActivityListModel stepsUp = new ActivityListModel(UtilsManager.getGenericId(), "Stairs up:", "");
		ActivityListModel stepsDown = new ActivityListModel(UtilsManager.getGenericId(), "Stairs down:", "");
		ActivityListModel sittingDown = new ActivityListModel(UtilsManager.getGenericId(), "Sitting down:", "");

		activityModels.add(running);
		activityModels.add(walking);
		activityModels.add(stepsUp);
		activityModels.add(stepsDown);
		activityModels.add(sittingDown);
		return activityModels;
	}

	public static MonitoringFragment newInstance(){
		return new MonitoringFragment();
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d("SENSORMANAGER", "register");
		initialTimeStamp = System.currentTimeMillis();
		if (mSensorProximity != null) {
			mSensorManager.registerListener(this, mSensorProximity,
					SensorManager.SENSOR_DELAY_NORMAL);
		}

		if (mAcceleroMeter != null) {
			mSensorManager.registerListener(this, mAcceleroMeter,
					SensorManager.SENSOR_DELAY_NORMAL);
		}

		if (mGyroScope != null) {
			mSensorManager.registerListener(this, mGyroScope,
					SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d("SENSORMANAGER", "unregister");
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();

		timeStamp = (System.currentTimeMillis() - initialTimeStamp)/1000.f;
		switch (sensorType) {
			// Event came from the light sensor.
			case Sensor.TYPE_PROXIMITY:
				float currentValue = event.values[0];
				Sensor sensorProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
				Long id = sensorDataIds.get(sensorProximity.getName());
				sensorAdapter.updateItem(id,
						String.valueOf(currentValue), true);
				if(chartModels != null){
					int position = getChartModelPosition(id);
					if(position != -1){
						ChartModel chartModel = chartModels.get(position);
						chartModel.updateValue(new Entry(timeStamp, currentValue));
						chartModels.set(position, chartModel);
					}
				}
				//Log.d("SENSORDATAAVAILABLE", "PROXIMITY value:" + currentValue);
				// Handle light sensor
				break;
			case Sensor.TYPE_ACCELEROMETER:
				float x_axis = event.values[0];
				float y_axis = event.values[1];
				float z_axis = event.values[2];
				Sensor sensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				sensorAdapter.updateItem(sensorDataIds.get(sensorAccelerometer.getName()), "x:"
						+ UtilsManager.round(x_axis, 1) + '\n' + "y:" +
						UtilsManager.round(y_axis, 1) + '\n' + "z:" +
						UtilsManager.round(z_axis, 1), false);
				//Log.d("SENSORDATAAVAILABLE", "accelerometer x:" + x_axis + " y:" + y_axis +
				//" z:" + z_axis);
				break;
			case Sensor.TYPE_GYROSCOPE:
				break;
			default:
				// do nothing
		}

		updateChart();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
