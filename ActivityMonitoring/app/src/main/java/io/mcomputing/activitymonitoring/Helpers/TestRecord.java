package io.mcomputing.activitymonitoring.Helpers;

public class TestRecord extends Record{
	int predictedLabel;

	TestRecord(double[] attributes, int classLabel) {
		super(attributes, classLabel);
	}
}