package com.poc.instagrampoc.imageloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.poc.instagrampoc.R;

public class ImageLoader {
	 String _type;
	MemoryCache memoryCache=new MemoryCache();
	FileCache fileCache;
	private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;
	Handler handler=new Handler();//handler to display images in UI thread

	public ImageLoader(Context context){
		fileCache=new FileCache(context);
		executorService=Executors.newFixedThreadPool(5);
	}

	final int stub_id=R.drawable.loading;
	public void DisplayImage(String url, String type, ImageView imageView)
	{
		Log.e("anirudh", "type"+type);
		imageViews.put(imageView, url);
		Bitmap bitmap=memoryCache.get(url);
		if(bitmap!=null)
			imageView.setImageBitmap(bitmap);
		else
		{
			queuePhoto(url, imageView,type);
			imageView.setImageResource(stub_id);
		}
	}

	private void queuePhoto(String url, ImageView imageView, String type)
	{
		PhotoToLoad p=new PhotoToLoad(url, imageView,type);
		executorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url, String _t) 
	{
		File f=fileCache.getFile(url);

		//from SD cache
		Bitmap b = decodeFile(f,_t);
		if(b!=null)
			return b;

		//from web
		try {
			Bitmap bitmap=null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is=conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			conn.disconnect();
			bitmap = decodeFile(f,_t);
			return bitmap;
		} catch (Throwable ex){
			ex.printStackTrace();
			if(ex instanceof OutOfMemoryError)
				memoryCache.clear();
			return null;
		}
	}

	//decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f, String _t){
		try {
			//decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1=new FileInputStream(f);
			BitmapFactory.decodeStream(stream1,null,o);
			stream1.close();

			//Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE=300;
			int width_tmp=o.outWidth, height_tmp=o.outHeight;
			int scale=1;
			while(true){
				if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
					break;
				width_tmp/=2;
				height_tmp/=2;
				scale*=2;
			}

			//decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize=scale;
			FileInputStream stream2=new FileInputStream(f);
			Bitmap bitmap=BitmapFactory.decodeStream(stream2, null, o2);
			stream2.close();

			if(_t.equalsIgnoreCase("rounded")){
				Log.e("anirudh", "type"+_type);
				return transform(bitmap);
			}else if(_t.equalsIgnoreCase("rect")){
				return bitmap;
			}

			
		} catch (FileNotFoundException e) {
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public Bitmap transform(Bitmap source) {

		int targetWidth = 100;
		int targetHeight = 100;
		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,targetHeight,Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(((float) targetWidth - 1) / 2,((float) targetHeight - 1) / 2,(Math.min(((float) targetWidth),((float) targetHeight)) / 2),Path.Direction.CCW);
		Paint paint = new Paint();
		paint.setColor(Color.GRAY);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setFilterBitmap(true);
		canvas.drawOval(new RectF(0, 0, targetWidth, targetHeight), paint) ;
		canvas.clipPath(path);
		Bitmap sourceBitmap = source;
		canvas.drawBitmap(sourceBitmap, new Rect(0,0,sourceBitmap.getWidth(),sourceBitmap.getHeight()),new Rect(0, 0, targetWidth,targetHeight), paint);
		return targetBitmap;


	}
	//Task for the queue
	private class PhotoToLoad
	{
		public String _t;
		public String url;
		public ImageView imageView;
		public PhotoToLoad(String u, ImageView i, String type){
			url=u; 
			imageView=i;
			_t = type;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;
		PhotosLoader(PhotoToLoad photoToLoad){
			this.photoToLoad=photoToLoad;
		}

		@Override
		public void run() {
			try{
				if(imageViewReused(photoToLoad))
					return;
				Bitmap bmp=getBitmap(photoToLoad.url,photoToLoad._t);
				memoryCache.put(photoToLoad.url, bmp);
				if(imageViewReused(photoToLoad))
					return;
				BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
				handler.post(bd);
			}catch(Throwable th){
				th.printStackTrace();
			}
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad){
		String tag=imageViews.get(photoToLoad.imageView);
		if(tag==null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	//Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable
	{
		Bitmap bitmap;
		PhotoToLoad photoToLoad;
		public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
		public void run()
		{
			if(imageViewReused(photoToLoad))
				return;
			if(bitmap!=null)
				photoToLoad.imageView.setImageBitmap(bitmap);
			else
				photoToLoad.imageView.setImageResource(R.drawable.ic_launcher);
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

}
