package io.mcomputing.activitymonitoring.Fragments;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.mcomputing.activitymonitoring.Adapters.MonitoringAdapter;
import io.mcomputing.activitymonitoring.Adapters.SensorDataAdapter;
import io.mcomputing.activitymonitoring.Models.ActivityListModel;
import io.mcomputing.activitymonitoring.Models.ChartModel;
import io.mcomputing.activitymonitoring.R;
import io.mcomputing.activitymonitoring.UtilsManager;

public class ProbabilityFragment extends Fragment {
	public static final String TAG = "ProbabilityFragment";
	private View view;
	private FragmentActivity activity;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		view = inflater.inflate(R.layout.probability_layout, null);
		activity = getActivity();
		setListView();
		return view;
	}

	public static ProbabilityFragment newInstance(){
		return new ProbabilityFragment();
	}

	private void setListView(){
		List<ActivityListModel> monitoringModels = generateItemList();
		MonitoringAdapter adapter = new MonitoringAdapter(activity, monitoringModels);
		ListView itemsListView  = (ListView) view.findViewById(R.id.probability_listview);
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


}
