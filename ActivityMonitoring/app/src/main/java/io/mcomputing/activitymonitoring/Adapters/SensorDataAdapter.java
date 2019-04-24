package io.mcomputing.activitymonitoring.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.zip.Inflater;

import io.mcomputing.activitymonitoring.Models.ActivityModel;
import io.mcomputing.activitymonitoring.R;

public abstract class SensorDataAdapter extends RecyclerView.Adapter<SensorDataAdapter.ViewHolder> {
	private final LayoutInflater inflater;
	private List<ActivityModel> _values;
	private Context _context;
	private static final int WAIT_TIME = 1;
	public SensorDataAdapter(Context context, List<ActivityModel> values){
		_context = context;
		_values = values;
		inflater = (LayoutInflater.from(context));
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		View view = inflater.inflate(R.layout.sensordata_layout, null);
		return new ViewHolder(view);
	}

	public void updateItem(Long id, String value, boolean isInstant){
		int position = 0;
		ActivityModel searchModel  = null;
		for(ActivityModel model: _values){
			if(model.getId().equals(id)){
				searchModel = model;
				break;
			}
			position++;
		}
		if(searchModel == null)
			return;

		if(((System.currentTimeMillis() - searchModel.getTimeStamp())/1000) > WAIT_TIME || isInstant) {
			_values.get(position).setValue(value);
			_values.get(position).setTimeStamp(System.currentTimeMillis());
			notifyItemChanged(position);
		}
	}

	public void updateOnClick(int position, boolean isVisible){
		_values.get(position).setVisible(isVisible);
		notifyItemChanged(position);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder holder, final int i) {
		ActivityModel activityModel = _values.get(i);

		holder.sensorName.setText(activityModel.getName());
		holder.sensorData.setText(activityModel.getValue());
		if(activityModel.isVisible())
			holder.itemView.setBackgroundColor(activityModel.getBackgroundColorActive());
		else
			holder.itemView.setBackgroundColor(activityModel.getBackgroundColorInActive());
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int position = holder.getAdapterPosition();
				Long id = _values.get(position).getId();
				boolean isVisible = _values.get(position).isVisible();
				onClickSensorData(id, !isVisible, position);
			}
		});
	}

	@Override
	public long getItemId(int position) {
		return _values.get(position).getId();
	}


	@Override
	public int getItemCount() {
		return _values.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		public TextView sensorName;
		public TextView sensorData;
		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			sensorName = itemView.findViewById(R.id.sensor_name);
			sensorData = itemView.findViewById(R.id.sensor_value);

		}
	}

	public abstract void onClickSensorData(Long id, boolean isVisible, int position);
}
