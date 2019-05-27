package io.mcomputing.activitymonitoring.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.File;

import cz.msebera.android.httpclient.Header;
import io.mcomputing.activitymonitoring.Helpers.DeleteCallback;
import io.mcomputing.activitymonitoring.JSONAsyncTask;
import io.mcomputing.activitymonitoring.R;

public class DeleteFeaturesDialog extends DialogFragment {
	public static final String TAG = "DeleteFeaturesDialog";
	private Activity activity;
	private DeleteCallback callback;

	public static DeleteFeaturesDialog newInstance(String title, String body, DeleteCallback callback) {
		DeleteFeaturesDialog frag = new DeleteFeaturesDialog();
		frag.callback = callback;
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("body", body);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public final void onAttach(final Context pContext) {
		// Handle as usual.
		super.onAttach(pContext);
		if (pContext instanceof Activity) {
			this.activity = (Activity) pContext;
		} else {
			this.activity = getActivity();
		}
	}

	@Deprecated
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String title = getArguments().getString("title");
		String body = getArguments().getString("body");
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(title);
		builder.setMessage(body);
		builder.setPositiveButton(getString(R.string.delete_txt), new DialogInterface.OnClickListener() {

			public void onClick(final DialogInterface dialog, int which) {
				JSONAsyncTask.resetFeatureExtraction(new JsonHttpResponseHandler(){
					@Override
					public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
						// If the response is JSONObject instead of expected JSONArray
						Log.d("RESETBODY", "success:" + response);
						final String filePath1 = activity.getFilesDir().getPath() + "/Final/train_features.csv";
						final String filePath2 = activity.getFilesDir().getPath() + "/Final/test_features.csv";
						File trainFile = new File(filePath1);
						if(trainFile.exists()){
							trainFile.delete();
						}
						File testFile = new File(filePath2);
						if(testFile.exists()){
							testFile.delete();
						}
						callback.onDeleted();

					}

					@Override
					public void onFailure(int statusCode, Header[] headers, Throwable throwable,
										  JSONObject errorResponse){
						// Pull out the first event on the public timeline
						Log.d("RESETBODY", "error:" + errorResponse);

						callback.onDeletedFailed();
					}

					@Override
					public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
						Log.d("RESETBODY", "error1:" + responseString);
						callback.onDeletedFailed();
					}
				});
				dialog.dismiss();

			}
		});

		builder.setNegativeButton(R.string.cancel_txt, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Do nothing
				dialog.dismiss();
			}
		});
		AlertDialog dialog = builder.create();

		dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(android.content.DialogInterface dialog,
								 int keyCode, android.view.KeyEvent event) {
				if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
					// To dismiss the fragment when the back-button is pressed.
					dismiss();
					return true;
				}
				// Otherwise, do nothing else
				else return false;
			}
		});
		return dialog;
	}
}