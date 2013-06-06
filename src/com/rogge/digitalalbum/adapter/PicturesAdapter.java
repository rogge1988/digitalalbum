package com.rogge.digitalalbum.adapter;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rogge.components.BitmapHelper;
import com.rogge.digitalalbum.R;
import com.rogge.digitalalbum.Settings;

/**
 * 图片adapter
 * @author zengsc
 * @date 2013-6-2
 */
public class PicturesAdapter extends PagerAdapter {
	static final String TAG = "PicturesAdapter";
	private Context mContext;
	private NodeList mNodeList;
	private BitmapHelper mBitmapHelper;

	public PicturesAdapter(Context context) {
		mContext = context;
		mBitmapHelper = BitmapHelper.getInstance();
	}

	/**
	 * 设置数据
	 */
	public void setData(NodeList list) {
		mNodeList = list;
		notifyDataSetChanged();
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public int getCount() {
		if (mNodeList == null)
			return 0;
		return mNodeList.getLength();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.getTag() == object;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Node item = mNodeList.item(position);
		String path = item.getFirstChild().getNodeValue();
		if (Settings.DEBUG) {
			Log.i(TAG, "instantiateItem:" + position);
			Log.i(TAG, "picture path:" + path);
		}
		View child = View.inflate(mContext, R.layout.picture_item, null);
		container.addView(child);
		child.setTag(item);
		ImageView picture = (ImageView) child.findViewById(R.id.picture);
		Bitmap bitmap = mBitmapHelper.getBitmap(path);
		picture.setImageBitmap(bitmap);
		return item;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		if (Settings.DEBUG) {
			Log.i(TAG, "destroyItem:" + position);
		}
		int index = -1;
		for (int i = 0; i < container.getChildCount(); i++) {
			if (container.getChildAt(i).getTag() == object) {
				index = i;
				break;
			}
		}
		if (index >= 0) {
			container.removeViewAt(index);
		}
	}
}
