package com.rogge.digitalalbum;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.rogge.components.BitmapHelper;
import com.rogge.components.Recorder;
import com.rogge.components.RemainingTimeCalculator;
import com.rogge.digitalalbum.adapter.PicturesAdapter;

/**
 * album activity
 * @author zengsc
 * @date 2013-6-2
 */
public class AlbumActivity extends Activity implements View.OnClickListener, View.OnTouchListener,
		ViewPager.OnPageChangeListener, Recorder.OnStateChangedListener, MediaPlayer.OnCompletionListener,
		MediaPlayer.OnErrorListener {
	public static final String TAG = "AlbumActivity";
	public static final boolean DEBUG = Settings.DEBUG;
	public static final int PICTURE_INDEX_REUSLT = 1;
	private int mSmallWidth = 138;
	private int mSmallHeight = 138;
	private BitmapHelper mBitmapHelper;
	private AlbumApplication mApplication;
	private int mAlbumIndex;

	private View mTopLayout;
	private View mMiddleLayout;
	private View mBottomLayout;
	private LinearLayout mAlbumsContainer;
	private LinearLayout mPicturesContainer;

	private ViewPager mViewPager;
	private PicturesAdapter mPagerAdapter;
	private ImageButton mPreviousButton;
	private ImageButton mNextButton;
	private ImageButton mBackButton;
	private ImageButton mPlayButton;
	private ImageButton mRecordButton;
	private ImageButton mStopButton;
	private ImageButton mDeleteButton;
	private ImageButton mShareButton;

	static final String AUDIO_3GPP = "audio/3gpp";
	static final String AUDIO_AMR = "audio/amr";
	static final String AUDIO_ANY = "audio/*";
	static final String ANY_ANY = "*/*";
	static final int BITRATE_AMR = 5900; // bits/sec
	static final int BITRATE_3GPP = 5900;
	private String mRequestedType = AUDIO_3GPP;
	private Recorder mRecorder;
	private MediaPlayer mPlayer;
	private RemainingTimeCalculator mRemainingTimeCalculator;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Settings.setFullscreenAndWake(this);

		// init data
		mSmallWidth = Settings.screenWidth / 4;
		mSmallHeight = Settings.screenHeight / 4;
		mSmallWidth = mSmallWidth > mSmallHeight ? mSmallHeight : mSmallWidth;
		mBitmapHelper = BitmapHelper.getInstance();
		mApplication = ((AlbumApplication) getApplication());

		// views
		setContentView(R.layout.album);
		mTopLayout = findViewById(R.id.top_layout);
		mMiddleLayout = findViewById(R.id.middle_layout);
		mBottomLayout = findViewById(R.id.bottom_layout);
		mAlbumsContainer = (LinearLayout) findViewById(R.id.albums);
		mPicturesContainer = (LinearLayout) findViewById(R.id.pictures);
		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mPreviousButton = (ImageButton) findViewById(R.id.previous_picture);
		mNextButton = (ImageButton) findViewById(R.id.next_picture);
		mBackButton = (ImageButton) findViewById(R.id.back_button);
		mPlayButton = (ImageButton) findViewById(R.id.play_button);
		mRecordButton = (ImageButton) findViewById(R.id.record_button);
		mStopButton = (ImageButton) findViewById(R.id.stop_button);
		mDeleteButton = (ImageButton) findViewById(R.id.delete_button);
		mShareButton = (ImageButton) findViewById(R.id.share_button);

		// albums
		mAlbumsContainer.removeAllViews();
		NodeList nodeList = mApplication.getDocument().getDocumentElement().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node album = nodeList.item(i);
			if (album != null && album.hasChildNodes()) {
				String path = album.getFirstChild().getFirstChild().getNodeValue();
				if (!TextUtils.isEmpty(path)) {
					Bitmap bitmap = mBitmapHelper.getBitmap(path, mSmallWidth, mSmallHeight);
					if (bitmap != null) {
						ImageView albumImage = new ImageView(this);
						albumImage.setBackgroundResource(R.drawable.item_background);
						albumImage.setImageBitmap(bitmap);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mSmallWidth, mSmallHeight);
						mAlbumsContainer.addView(albumImage, params);
						albumImage.setOnClickListener(this);
					}
				}
			}
		}

		// viewpager
		mPagerAdapter = new PicturesAdapter(this);
		mViewPager.setAdapter(mPagerAdapter);

		// event listener
		mTopLayout.setOnTouchListener(this);
		mMiddleLayout.setOnTouchListener(this);
		mBottomLayout.setOnTouchListener(this);
		mViewPager.setOnTouchListener(this);
		mViewPager.setOnPageChangeListener(this);
		mPreviousButton.setOnClickListener(this);
		mNextButton.setOnClickListener(this);
		mBackButton.setOnClickListener(this);
		mPlayButton.setOnClickListener(this);
		mRecordButton.setOnClickListener(this);
		mStopButton.setOnClickListener(this);
		mDeleteButton.setOnClickListener(this);
		mShareButton.setOnClickListener(this);

		// recorder
		mRecorder = new Recorder();
		mRecorder.setOnStateChangedListener(this);
		mRemainingTimeCalculator = new RemainingTimeCalculator();

		// default index
		Intent i = getIntent();
		int index = i.getIntExtra(Settings.ALBUM_INDEX, 0);
		setAlbum(index);
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
	public void setAlbum(int index) {
		if (DEBUG)
			Log.d(TAG, "set album:" + index);
		mAlbumIndex = index;
		Node node = mApplication.getDocument().getDocumentElement().getChildNodes().item(index);
		NodeList nodeList = node.getChildNodes();
		// pictures
		mPicturesContainer.removeAllViews();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node picture = nodeList.item(i);
			if (picture != null && picture.hasChildNodes()) {
				String path = picture.getFirstChild().getNodeValue();
				if (!TextUtils.isEmpty(path)) {
					Bitmap bitmap = mBitmapHelper.getBitmap(path, mSmallWidth, mSmallHeight);
					if (bitmap != null) {
						ImageView pictureImage = new ImageView(this);
						pictureImage.setBackgroundResource(R.drawable.item_background);
						pictureImage.setImageBitmap(bitmap);
						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mSmallWidth, mSmallHeight);
						mPicturesContainer.addView(pictureImage, params);
						pictureImage.setOnClickListener(this);
					}
				}
			}
		}
		mPagerAdapter.setData(nodeList);
		setPicture(0);
		resetUi();
	}

	private void resetUi() {
		mRecorder.clear();
		stopPlaying();
		updateUi(Recorder.IDLE_STATE);
	}

	private void updateUi(int state) {
		boolean canTouch = state == Recorder.IDLE_STATE;
		mTopLayout.setEnabled(canTouch);
		mMiddleLayout.setEnabled(canTouch);
		mBottomLayout.setEnabled(canTouch);
		boolean canPlay = false;
		if (state == Recorder.IDLE_STATE) {
			String path = getRecordPath();
			if (!TextUtils.isEmpty(path)) {
				File file = new File(path);
				if (file.exists())
					canPlay = true;
			}
		}
		mPlayButton.setEnabled(canPlay);
		boolean canRecord = state != Recorder.RECORDING_STATE;
		mRecordButton.setEnabled(canRecord);
		boolean canStop = state != Recorder.IDLE_STATE;
		mStopButton.setEnabled(canStop);
		mDeleteButton.setEnabled(canPlay);
	}

	/**
	 * jump to index picture
	 */
	public void setPicture(int index) {
		mViewPager.setCurrentItem(index);
	}

	private Node getCurrentNode() {
		try {
			Node node = mApplication.getDocument().getDocumentElement().getChildNodes().item(mAlbumIndex);
			NodeList nodeList = node.getChildNodes();
			Node item = nodeList.item(mViewPager.getCurrentItem());
			return item;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getRecordPath() {
		Element item = (Element) getCurrentNode();
		if (item == null || !item.hasAttribute(Settings.RECORD_NAME))
			return null;
		return item.getAttribute(Settings.RECORD_NAME);
	}

	private void setRecordPath(String path) {
		Element item = (Element) getCurrentNode();
		if (item == null)
			return;
		item.setAttribute(Settings.RECORD_NAME, path);
	}

	private void deleteRecordPath() {
		Element item = (Element) getCurrentNode();
		item.removeAttribute(Settings.RECORD_NAME);
	}

	private float mX;
	private float mY;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v == mViewPager) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				mX = event.getX();
				mY = event.getY();
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				if (event.getX() == mX && event.getY() == mY) {
					Intent intent = new Intent();
					intent.setClass(getApplicationContext(), AlbumFullActivity.class);
					intent.putExtra(Settings.ALBUM_INDEX, mAlbumIndex);
					intent.putExtra(Settings.PICTURE_INDEX, mViewPager.getCurrentItem());
					startActivityForResult(intent, PICTURE_INDEX_REUSLT);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if (v == mPreviousButton) {
			mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
		} else if (v == mNextButton) {
			mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
		} else if (v == mBackButton) {
			finish();
		} else if (v == mPlayButton) {
			play();
		} else if (v == mRecordButton) {
			delete();
			startRecord();
		} else if (v == mStopButton) {
			stop();
		} else if (v == mDeleteButton) {
			delete();
		} else if (v instanceof ImageView) {
			int index = mAlbumsContainer.indexOfChild(v);
			if (index >= 0) {
				setAlbum(index);
			} else {
				index = mPicturesContainer.indexOfChild(v);
				if (index >= 0) {
					setPicture(index);
				}
			}
		}
	}

	// play record if exist
	private void play() {
		mRecorder.stop();
		String recordPath = getRecordPath();
		if (TextUtils.isEmpty(recordPath))
			return;
		File recordFile = new File(recordPath);
		if (!recordFile.exists())
			return;
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(recordFile.getAbsolutePath());
			mPlayer.setOnCompletionListener(this);
			mPlayer.setOnErrorListener(this);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			mPlayer = null;
			updateUi(Recorder.IDLE_STATE);
			return;
		} catch (IOException e) {
			mPlayer = null;
			updateUi(Recorder.IDLE_STATE);
			return;
		}
		updateUi(Recorder.PLAYING_STATE);
	}

	// start record
	private void startRecord() {
		mRemainingTimeCalculator.reset();
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			if (DEBUG)
				Log.w(TAG, "insert sd card!");
			updateUi(Recorder.IDLE_STATE);
		} else if (!mRemainingTimeCalculator.diskSpaceAvailable()) {
			if (DEBUG)
				Log.w(TAG, "storage is full!");
			updateUi(Recorder.IDLE_STATE);
		} else {
			stopAudioPlayback();

			if (AUDIO_AMR.equals(mRequestedType)) {
				mRemainingTimeCalculator.setBitRate(BITRATE_AMR);
				mRecorder.startRecording(MediaRecorder.OutputFormat.AMR_NB, ".amr", this);
			} else if (AUDIO_3GPP.equals(mRequestedType)) {
				mRemainingTimeCalculator.setBitRate(BITRATE_3GPP);
				mRecorder.startRecording(MediaRecorder.OutputFormat.THREE_GPP, ".3gpp", this);
			} else {
				throw new IllegalArgumentException("Invalid output file type requested");
			}
			updateUi(Recorder.RECORDING_STATE);
		}
	}

	// stop playing and recording
	private void stop() {
		if (mRecorder.state() == Recorder.RECORDING_STATE) {
			String path = mRecorder.save();
			if (DEBUG)
				Log.i(TAG, "save record:" + path);
			setRecordPath(path);
		}
		stopPlaying();
		updateUi(Recorder.IDLE_STATE);
	}

	// stop playing
	private void stopPlaying() {
		if (mPlayer == null) // we were not in playback
			return;
		mPlayer.stop();
		mPlayer.release();
		mPlayer = null;
	}

	private void delete() {
		String recordPath = getRecordPath();
		if (!TextUtils.isEmpty(recordPath)) {
			deleteRecordPath();
			File recordFile = new File(recordPath);
			if (recordFile.exists())
				recordFile.delete();
		}
		updateUi(Recorder.IDLE_STATE);
	}

	// stop playback
	private void stopAudioPlayback() {
		// Shamelessly copied from MediaPlaybackService.java, which should be public, but isn't.
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		sendBroadcast(i);
	}

	@Override
	public void onStateChanged(int state) {
	}

	@Override
	public void onError(int error) {
		String message = "record error:" + error;
		if (DEBUG)
			Log.w(TAG, message);
		new AlertDialog.Builder(this).setTitle(R.string.app_name).setMessage(message)
				.setPositiveButton(android.R.string.ok, null).setCancelable(false).show();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		stopPlaying();
		updateUi(Recorder.IDLE_STATE);
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		stopPlaying();
		updateUi(Recorder.IDLE_STATE);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		if (DEBUG)
			Log.d(TAG, "onPageScrollStateChanged:" + state);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		if (DEBUG)
			Log.i(TAG, "onPageSelected:" + position);
		updateUi(Recorder.IDLE_STATE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PICTURE_INDEX_REUSLT) {
			int index = data.getIntExtra(Settings.PICTURE_INDEX, 0);
			setPicture(index);
		}
	}
}
