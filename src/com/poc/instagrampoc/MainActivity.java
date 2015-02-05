package com.poc.instagrampoc;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.poc.instagrampoc.Model.ImageBean;
import com.poc.instagrampoc.adapter.ImageAdapter;
import com.poc.instagrampoc.webservices.HttpPostListener;
import com.poc.instagrampoc.webservices.HttpPostMethodCalls;

public class MainActivity extends Activity implements HttpPostListener{

	private GridView grid;
	private ArrayList<ImageBean> images_list = new ArrayList<ImageBean>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		grid = (GridView) findViewById(R.id.grid);

		try {
			Log.i("VRV", "In oncreate");
			String classname = "MainActivity";
			String method = "images";
			String url = "https://api.instagram.com/v1/tags/selfie/media/recent?access_token=1516421358.ca0f574.3c054818208147a6aca965444b7a0d98";
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			HttpPostMethodCalls sTask = new HttpPostMethodCalls(MainActivity.this,url,urlParameters,method,classname);
			sTask.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getResponse(String data, String method, String classname) {
		if (classname.equalsIgnoreCase("MainActivity")) {
			if (method.equalsIgnoreCase("images")) {
				try {
					images_list.clear();
					JSONObject Object = new JSONObject(data);
					JSONArray ImageArray = Object.getJSONArray("data");
					for (int i = 0; i < ImageArray.length(); i++) {
						JSONObject Images = ImageArray.getJSONObject(i);
						JSONObject Image = Images.getJSONObject("images");
						JSONObject thunmbnail = Image.getJSONObject("thumbnail");
						String url1 = thunmbnail.getString("url");
						Log.i("VRV", "url1 ::"+url1);
						JSONObject normal = Image.getJSONObject("low_resolution");
						String url2 = normal.getString("url");
						JSONObject standerd = Image.getJSONObject("standard_resolution");
						String url3 = standerd.getString("url");
						
						ImageBean bean = new ImageBean();
						bean.setThumbnail(url1);
						bean.setNormal(url2);
						bean.setStanderd(url3);
						images_list.add(bean);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				ImageAdapter adapter = new ImageAdapter(MainActivity.this, images_list);
				grid.setAdapter(adapter);
				grid.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
						
						Intent i = new Intent(MainActivity.this, ImageActivity.class);
						i.putExtra("image", images_list.get(arg2).getStanderd());
						startActivity(i);
						
					}
				});
			}
		}

	}

}
