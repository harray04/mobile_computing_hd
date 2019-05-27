package io.mcomputing.activitymonitoring;

import android.content.Context;
import android.location.Location;
import android.util.Log;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.entity.StringEntity;


public class JSONAsyncTask extends JsonHttpResponseHandler {
	public static final String BASE_URL = "https://us-central1-coinz-c5130.cloudfunctions.net/train/";
	public static final String FIT = "fit";
	public static final String HAS_PROB = "prob";
	public static final String ACT = "act";
	public static final String RESETALL = "resetAll";
	public static final String RESETEXT = "resetExt";

	private static AsyncHttpClient client = new AsyncHttpClient(true,80,443);

	public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.post(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void post(Context context, String url, StringEntity entity, AsyncHttpResponseHandler responseHandler) {
		client.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler);
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

	public static void getFeature(RequestParams params, AsyncHttpResponseHandler responseHandler){
		JSONAsyncTask.post(HAS_PROB, params, responseHandler);
	}

	public static void setActivities(Context context, JSONArray array, AsyncHttpResponseHandler responseHandler) throws UnsupportedEncodingException {
		StringEntity stringEntity = new StringEntity(array.toString());
		JSONAsyncTask.post(context, ACT, stringEntity, responseHandler);
	}

	public static void getActivities(AsyncHttpResponseHandler responseHandler){
		JSONAsyncTask.get(ACT, null, responseHandler);
	}

	public static void trainFeature(String count, AsyncHttpResponseHandler responseHandler){
		JSONAsyncTask.get(count, null, responseHandler);
	}

	public static void resetAll(AsyncHttpResponseHandler responseHandler){
		JSONAsyncTask.put(RESETALL, null, responseHandler);
	}
	public static void resetFeatureExtraction(AsyncHttpResponseHandler responseHandler){
		JSONAsyncTask.put(RESETEXT, null, responseHandler);
	}


	public static void getExtracted(AsyncHttpResponseHandler responseHandler){
		JSONAsyncTask.get(FIT, null, responseHandler);
	}
}

