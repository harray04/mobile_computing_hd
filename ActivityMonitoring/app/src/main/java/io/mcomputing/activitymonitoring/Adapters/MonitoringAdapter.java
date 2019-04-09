package io.mcomputing.activitymonitoring.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import java.util.List;

import io.mcomputing.activitymonitoring.Models.MonitoringModel;

public class MonitoringAdapter extends BaseAdapter {
	private final List<MonitoringModel> values;

	public MonitoringAdapter(List<MonitoringModel> sensorData){
		values = sensorData;
	}

	@Override
	public int getCount() {
		return values.size();
	}

	@Override
	public Object getItem(int position) {
		return values.get(position);
	}

	@Override
	public long getItemId(int position) {
		return values.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return null;
	}
}
