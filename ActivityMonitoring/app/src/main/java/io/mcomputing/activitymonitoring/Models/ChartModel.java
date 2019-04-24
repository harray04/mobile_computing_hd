package io.mcomputing.activitymonitoring.Models;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class ChartModel {
	private Long _sensor_id;
	private String _label;
	private List<Entry> _value;
	private boolean _isVisible;
	private int _color;
	private int _cirecleColor;

	public Long getSensorId() {
		return _sensor_id;
	}

	public void setIsVisible(boolean isVisible) {
		this._isVisible = isVisible;
	}

	public boolean isVisible() {
		return _isVisible;
	}

	public String getLabel() {
		return _label;
	}

	public List<Entry> getValue() {
		return _value;
	}

	public void updateValue(Entry entry){
		if(_value == null)
			_value = new ArrayList<>();
		_value.add(entry);
	}

	public void resetValue(){
		if(_value != null)
			_value.clear();
	}
	public ChartModel(Long sensor_id,String label, List<Entry> value, int color, int cirecleColor){
		_sensor_id = sensor_id;
		_label = label;
		_value = value;
		_color = color;
		_cirecleColor = cirecleColor;
	}

	public int getColor() {
		return _color;
	}

	public int getCircleColor() {
		return _cirecleColor;
	}
}