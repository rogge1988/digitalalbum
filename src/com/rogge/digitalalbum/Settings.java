package com.rogge.digitalalbum;

import java.io.File;

import android.app.Activity;
import android.os.Environment;
import android.view.Window;
import android.view.WindowManager;

/**
 * 一些全局变量
 * @author Rogge
 * @date 2013-5-26
 */
public class Settings {
	public static final String DIGITAL_ALBUM_PATH = Environment.getExternalStorageDirectory() + File.separator
			+ "digitalalbum" + File.separator;
	public static final String DATA_PATH = DIGITAL_ALBUM_PATH + "data.xml";
	public static final String PICTURE_PATH = DIGITAL_ALBUM_PATH + "pictures";
	public static final String RECORD_PATH = DIGITAL_ALBUM_PATH + "records";
	// xml attribute name
	public static final String ALBUM_NAME = "album";
	public static final String PICTURE_NAME = "picture";
	public static final String RECORD_NAME = "record";
	// intent data name
	public static final String ALBUM_INDEX = "album_index";
	public static final String PICTURE_INDEX = "picture_index";
	// is debug
	public static final boolean DEBUG = true;
	public static int screenWidth;
	public static int screenHeight;

	// set full screen and wake
	public static void setActionBarAndWake(Activity activity) {
		// 设置actioinbar
		activity.requestWindowFeature(Window.FEATURE_ACTION_BAR);
		// 设置全屏
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}

	// set full screen and wake
	public static void setFullscreenAndWake(Activity activity) {
		// 设置无标题
		activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 设置全屏
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}
}
