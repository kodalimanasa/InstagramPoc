package com.poc.instagrampoc.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.poc.instagrampoc.R;
import com.poc.instagrampoc.Model.ImageBean;
import com.poc.instagrampoc.imageloader.ImageLoader;


public class ImageAdapter extends BaseAdapter{
	Context context;
	ArrayList<ImageBean> image_list;
	private int count = 0;
	public ImageAdapter(Context _context,ArrayList<ImageBean> _image_list) {
		context = _context;
		image_list = _image_list;
	}

	@Override
	public int getCount() {
		Log.i("VRV", "image_list.size() ::"+image_list.size());
		return image_list.size();
	}

	@Override
	public Object getItem(int arg0) {
		return image_list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View v, ViewGroup arg2) {
		final Holder holder;
		ImageLoader imageLoder = new ImageLoader(context);
		final ImageBean bean = image_list.get(arg0);
		if (v == null){                                    
			LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.image_display, null);
			holder = new Holder();
			holder.img = (ImageView) v.findViewById(R.id.img);
			v.setTag(holder);
		} 
		else{
			holder = (Holder) v.getTag(); 
		}
		v.setTag(holder);
		if (arg0%3 == 0) {
			try{
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(200, 200);
				holder.img.setLayoutParams(layoutParams);
				String image_url =image_list.get(arg0).getStanderd();
				imageLoder.DisplayImage(image_url,"rect", holder.img);
			}catch(Exception e){
				e.printStackTrace();
			}
		}else {
			try{
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(75,75);
				holder.img.setLayoutParams(layoutParams);
				String image_url =image_list.get(arg0).getThumbnail();
				imageLoder.DisplayImage(image_url,"rect", holder.img);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return v;
	}     
	class Holder{
		public ImageView img;            
	}
}
