package io.mcomputing.activitymonitoring.Models;

public class MonitoringModel {
	private Long _id;
	private String _value;


	public Long getId() {
		return _id;
	}

	public String getValue() {
		return _value;
	}

	public MonitoringModel(Long id, String value){
		_id = id;
		_value = value;
	}
}
