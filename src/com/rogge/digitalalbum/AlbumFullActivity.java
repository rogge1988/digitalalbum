package com.rogge.digitalalbum;

import org.w3c.dom.Node;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;

import com.rogge.digitalalbum.adapter.PicturesAdapter;

/**
 * 相册全屏
 * @author zengsc
 * @date 2013-6-3
 */
public class AlbumFullActivity extends Activity implements View.OnClickListener {
	private AlbumApplication mApplication;
	private ViewPager mViewPager;
	private ImageButton mBackButton;
	private PicturesAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Settings.setFullscreenAndWake(this);
		mApplication = ((AlbumApplication) getApplication());
		setContentView(R.layout.album_fullscreen);
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mBackButton = (ImageButton) findViewById(R.id.back_button);

		mPagerAdapter = new PicturesAdapter(this);
		mViewPager.setAdapter(mPagerAdapter);

		mBackButton.setOnClickListener(this);

		Intent i = getIntent();
		int index = i.getIntExtra(Settings.ALBUM_INDEX, 0);
		int index2 = i.getIntExtra(Settings.PICTURE_INDEX, 0);
		setIndex(index, index2);
	}

	@Override
	protected void onDestroy() {
		mApplication.saveDocument();
		mApplication = null;
		super.onDestroy();
	}

	/**
	 * jump to index album
	 */
	public void setIndex(int index, int index2) {
		Node node = mApplication.getDocument().getDocumentElement().getChildNodes().item(index);
		mPagerAdapter.setData(node.getChildNodes());
		mViewPager.setCurrentItem(index2);
	}

	@Override
	public void finish() {
		Intent data = new Intent();
		data.putExtra(Settings.PICTURE_INDEX, mViewPager.getCurrentItem());
		setResult(RESULT_OK, data);
		super.finish();
	}

	@Override
	public void onClick(View v) {
		if (v == mBackButton) {
			finish();
		}
	}
}
