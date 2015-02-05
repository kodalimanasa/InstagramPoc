package com.poc.instagrampoc;

import com.poc.instagrampoc.imageloader.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class ImageActivity extends Activity {

	private ImageView image;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		image = (ImageView) findViewById(R.id.image);
		String url = getIntent().getStringExtra("image");
		ImageLoader iLoder = new ImageLoader(ImageActivity.this);
		iLoder.DisplayImage(url, "rect", image);
	}

}
