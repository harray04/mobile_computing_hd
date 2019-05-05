package io.mcomputing.activitymonitoring.Helpers;

import android.icu.text.AlphabeticIndex;

public interface Metric {
	double getDistance(Record s, Record e);
}