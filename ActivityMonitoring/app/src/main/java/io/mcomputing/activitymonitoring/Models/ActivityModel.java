package io.mcomputing.activitymonitoring.Models;

import android.graphics.Color;

public class ActivityModel {
	private Long _id;
	private String _name;
	private String _value;
	private int _backgroundColorActive;
	private int _backgroundColorInActive;
	private boolean _isVisible;

	public void setBackgroundColorActive(int backgroundColor) {
		this._backgroundColorActive = backgroundColor;
	}

	public int getBackgroundColorActive() {
		return _backgroundColorActive;
	}

	public void setBackgroundColorInActive(int backgroundColor) {
		this._backgroundColorInActive = backgroundColor;
	}

	public int getBackgroundColorInActive() {
		return _backgroundColorInActive;
	}

	public boolean isVisible() {
		return _isVisible;
	}

	public void setValue(String value) {
		this._value = value;
	}

	public void setVisible(boolean visible) {
		_isVisible = visible;
	}

	public Long getId() {
		return _id;
	}

	public String getName() {
		return _name;
	}

	public String getValue() {
		return _value;
	}



	public ActivityModel(Long id, String name, String value, int activeColor, int inActiveColor){
		_id = id;
		_name = name;
		_value = value;
		_backgroundColorActive = activeColor;
		_backgroundColorInActive = inActiveColor;
	}
}

