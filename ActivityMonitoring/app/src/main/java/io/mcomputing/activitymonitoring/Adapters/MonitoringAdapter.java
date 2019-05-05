package io.mcomputing.activitymonitoring.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import io.mcomputing.activitymonitoring.Models.ActivityListModel;
import io.mcomputing.activitymonitoring.Models.ActivityModel;
import io.mcomputing.activitymonitoring.R;

public class MonitoringAdapter extends BaseAdapter {
	private final List<ActivityListModel> values;
	private final LayoutInflater inflater;

	public MonitoringAdapter(Context context, List<ActivityListModel> sensorData){
		values = sensorData;
		inflater = (LayoutInflater.from(context));
	}

	public void setItemAtPos(int pos, String value){
		values.get(pos).setValue(value);
		notifyDataSetChanged();
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
		ActivityListModel monitoringModel = values.get(position);
		if(convertView == null){
			convertView = inflater.inflate(R.layout.monitoring_adapter_layout, null);
		}


		TextView currentProbability = convertView.findViewById(R.id.am_current_probability);
		TextView amActivity = convertView.findViewById(R.id.am_activity);

		amActivity.setText(monitoringModel.getName());
		currentProbability.setText(monitoringModel.getValue()); // TODO

		return convertView;
	}
}
