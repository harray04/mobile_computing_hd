package io.mcomputing.activitymonitoring.Helpers;

public class Record {
	double[] attributes;
	int classLabel;

	Record(double[] attributes, int classLabel){
		this.attributes = attributes;
		this.classLabel = classLabel;
	}
}
