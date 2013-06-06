package com.rogge.components;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

/**
 * 读取图片helper
 * @author Rogge
 * @date 2013-5-26
 */
public class BitmapHelper {
	private static BitmapHelper mBitmapHelper;

	/**
	 * 获取单例
	 */
	public static BitmapHelper getInstance() {
		if (mBitmapHelper == null)
			mBitmapHelper = new BitmapHelper();
		return mBitmapHelper;
	}

	private Map<String, WeakReference<Bitmap>> mBitmapCache;
	private Map<String, WeakReference<Bitmap>> mThumbnailCache;

	private BitmapHelper() {
		mBitmapCache = new HashMap<String, WeakReference<Bitmap>>();
		mThumbnailCache = new HashMap<String, WeakReference<Bitmap>>();
	}

	/**
	 * 读取图片
	 */
	public Bitmap getBitmap(String path) {
		Bitmap bitmap = null;
		if (mBitmapCache.containsKey(path))
			bitmap = mBitmapCache.get(path).get();
		if (bitmap != null)
			return bitmap;
		mBitmapCache.remove(path);
		bitmap = BitmapFactory.decodeFile(path);
		mBitmapCache.put(path, new WeakReference<Bitmap>(bitmap));
		return bitmap;
	}

	/**
	 * 按指定尺寸读取图片
	 */
	public Bitmap getBitmap(String path, int width, int height) {
		String key = path + "_" + width + "x" + height;
		Bitmap bitmap = null;
		// 查看缩略图缓存
		if (mThumbnailCache.containsKey(key))
			bitmap = mThumbnailCache.get(key).get();
		if (bitmap != null)
			return bitmap;
		// 缩略图缓存中不存在
		mThumbnailCache.remove(key);
		// 查看原图缓存
		if (mBitmapCache.containsKey(path))
			bitmap = mBitmapCache.get(path).get();
		if (bitmap != null) {
			// 原图存在则压缩成缩略图
			// bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height);
			bitmap = getBitmap(bitmap, width, height);
		} else {
			// 原图也不存在，则读缩略图
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// 获取这个图片的宽和高，注意此处的bitmap为null
			BitmapFactory.decodeFile(path, options);
			// 计算缩放比
			int h = options.outHeight;
			int w = options.outWidth;
			int scaleX = w / width;
			int scaleY = h / height;
			int scale = 1;
			scale = scaleX > scaleY ? scaleY : scaleX;
			options.inJustDecodeBounds = false; // 设为 false
			options.inSampleSize = scale;
			// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
			bitmap = BitmapFactory.decodeFile(path, options);
			System.err.println(bitmap + "width:" + bitmap.getWidth());
			System.err.println(bitmap + "height:" + bitmap.getHeight());
			// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
			// bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
			bitmap = getBitmap(bitmap, width, height);
		}
		// 将缩略图放入缓存
		mThumbnailCache.put(key, new WeakReference<Bitmap>(bitmap));
		return bitmap;
	}

	/**
	 * 转换成不大于指定尺寸的bitmap
	 */
	public Bitmap getBitmap(Bitmap bitmap, int width, int height) {
		if (bitmap == null)
			return null;
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		if (width == w && height == h)
			return bitmap;
		float scaleX = 1.0f * width / w;
		float scaleY = 1.0f * height / h;
		float scale = scaleX > scaleY ? scaleY : scaleX;
		if (scale != 1) {
			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		}
		return bitmap;
	}
}
