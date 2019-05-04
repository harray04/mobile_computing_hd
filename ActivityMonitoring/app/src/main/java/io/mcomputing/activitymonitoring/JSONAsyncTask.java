package io.mcomputing.activitymonitoring;

import android.location.Location;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;


public class JSONAsyncTask extends JsonHttpResponseHandler {
	public static final String BASE_URL = "https://us-central1-coinz-c5130.cloudfunctions.net/train/";
	public static final String HAS_FIT = "fit";
	public static final String HAS_PROB = "prob";

	private static AsyncHttpClient client = new AsyncHttpClient(true,80,443);

	public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.post(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void put(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.put(getAbsoluteUrl(url), params, responseHandler);
	}

	private static String getAbsoluteUrl(String relativeUrl) {
		return BASE_URL + relativeUrl;
	}

	public static void hasProb(AsyncHttpResponseHandler responseHandler){
		JSONAsyncTask.get(HAS_PROB, null, responseHandler);
	}
}

