package com.poc.instagrampoc.webservices;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class HttpPostMethodCalls extends AsyncTask<Void, Void, String>{
	Context context;
	HttpPostListener listener;
	String url;
	List<NameValuePair> urlParameters;
	String method;
	String classname;
	public HttpPostMethodCalls(Context _context, String _url,List<NameValuePair> _urlParameters, String _method, String _classname) {
		context = _context;
		url = _url;
		urlParameters = _urlParameters;
		method = _method;
		classname = _classname;
	}

	@Override                                 
	protected void onPreExecute() {
		super.onPreExecute();
		//TTGTProgressDialog.startProgress(context);               
	}           

	@Override
	protected String doInBackground(Void... params) {
		try {
			listener = (HttpPostListener) context;
		} catch (Exception e) {
			Log.i("VRV", "Exception is"+e.getMessage());
		}
		
		String responseBody = null;
		HttpClient httpclient = new DefaultHttpClient();   
		HttpGet httpget = new HttpGet(url);
		//HttpPost httppost = new HttpPost(url);
		try {
			//httppost.setEntity(new UrlEncodedFormEntity(urlParameters));
			HttpResponse response = httpclient.execute(httpget);
			responseBody = EntityUtils.toString(response.getEntity());
			Log.i("VRV", "In HttpPostMethodCalls Response is ::"+responseBody);
			return responseBody;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return responseBody;
	}  

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		//TTGTProgressDialog.stopProgress(context);
		listener.getResponse(result,method,classname);
		
	}


}
