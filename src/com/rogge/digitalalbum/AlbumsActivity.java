package com.rogge.digitalalbum;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rogge.components.BitmapHelper;

/**
 * album list activity
 * @author Rogge
 * @date 2013-5-26
 */
public class AlbumsActivity extends Activity implements MenuItem.OnMenuItemClickListener, View.OnClickListener {
	private BitmapHelper mBitmapHelper;
	private AlbumApplication mApplication;
	private MenuItem mSettings;

	private HorizontalScrollView mHorizontalScrollView;
	private LinearLayout mHorizontalContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Settings.setActionBarAndWake(this);
		final String pictures = "pictures" + File.separator;
		String[] paths = null;
		try {
			paths = getAssets().list("pictures");
		} catch (IOException e) {
			e.printStackTrace();
		}
		String outFileName = Settings.DIGITAL_ALBUM_PATH + pictures;
		File file = new File(outFileName);
		if (!file.exists())
			file.mkdirs();
		InputStream myInput = null;
		OutputStream myOutput = null;
		for (String path : paths) {
			try {
				File outputFile = new File(outFileName + path);
				if (outputFile.exists())
					continue;
				myInput = getAssets().open(pictures + path);
				myOutput = new FileOutputStream(outputFile);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = myInput.read(buffer)) > 0) {
					myOutput.write(buffer, 0, length);
				}
				myOutput.flush();
				myInput.close();
				myOutput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// setContentView(R.layout.activity_main);
		mHorizontalScrollView = new HorizontalScrollView(this);
		setContentView(mHorizontalScrollView);
		mHorizontalScrollView.setHorizontalScrollBarEnabled(false);
		mHorizontalScrollView.setVerticalScrollBarEnabled(false);
		// get screen size
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		Settings.screenWidth = dm.widthPixels;
		Settings.screenHeight = dm.heightPixels;

		mHorizontalContainer = new LinearLayout(this);
		mHorizontalScrollView.addView(mHorizontalContainer);
		mHorizontalContainer.setOrientation(LinearLayout.HORIZONTAL);
		mHorizontalContainer.setGravity(Gravity.CENTER_VERTICAL);
		mBitmapHelper = BitmapHelper.getInstance();

		// init data
		mApplication = ((AlbumApplication) getApplication());
		NodeList nodeList = mApplication.getDocument().getDocumentElement().getChildNodes();
		mHorizontalContainer.removeAllViews();
		int width = Settings.screenWidth / 3;
		int height = Settings.screenHeight;
		height = height > width ? width : height;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node album = nodeList.item(i);
			if (album != null && album.hasChildNodes()) {
				String path = album.getFirstChild().getFirstChild().getNodeValue();
				if (!TextUtils.isEmpty(path)) {
					Bitmap bitmap = mBitmapHelper.getBitmap(path, width, height);
					if (bitmap != null) {
						ImageView albumImage = new ImageView(this);
						albumImage.setImageBitmap(bitmap);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
						mHorizontalContainer.addView(albumImage, params);
						albumImage.setOnClickListener(this);
					}
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		mApplication.saveDocument();
		mApplication = null;
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		mSettings = menu.findItem(R.id.action_settings);
		mSettings.setOnMenuItemClickListener(this);
		mSettings.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (item == mSettings) {
			startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)); // 直接进入手机中的wifi网络设置界面
			return true;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v instanceof ImageView) {
			int index = mHorizontalContainer.indexOfChild(v);
			if (index >= 0) {
				viewAlbum(index);
			}
		}
	}

	/**
	 * view album
	 */
	public void viewAlbum(int index) {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), AlbumActivity.class);
		intent.putExtra(Settings.ALBUM_INDEX, index);
		startActivity(intent);
	}
}
