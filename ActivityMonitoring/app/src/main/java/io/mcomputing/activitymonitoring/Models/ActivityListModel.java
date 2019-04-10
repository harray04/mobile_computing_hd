package io.mcomputing.activitymonitoring.Models;

import android.graphics.Color;

public class ActivityListModel {
	private Long _id;
	private String _name;
	private String _value;

	public void setValue(String value) {
		this._value = value;
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



	public ActivityListModel(Long id, String name, String value){
		_id = id;
		_name = name;
		_value = value;
	}
}


