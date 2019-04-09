package io.mcomputing.activitymonitoring.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.mcomputing.activitymonitoring.R;

public class DigitsFragment extends Fragment {


	public static final String TAG = "DigitsFragment";
	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);

		view = inflater.inflate(R.layout.digits_layout, null);

		return view;
	}

	public static DigitsFragment newInstance(){
		return new DigitsFragment();
	}
}
