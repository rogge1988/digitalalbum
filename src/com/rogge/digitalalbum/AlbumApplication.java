package com.rogge.digitalalbum;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;

import android.app.Application;

import com.rogge.components.DomXMLHelper;

/**
 * 相册应用
 * @author Rogge
 * @date 2013-5-26
 */
public class AlbumApplication extends Application {
	private Document mDocument;

	/**
	 * 获得数据
	 */
	public Document getDocument() {
		if (mDocument == null) {
			mDocument = DomXMLHelper.parseXML(Settings.DATA_PATH);
			if (mDocument == null) {
				InputStream is = null;
				try {
					is = getAssets().open("data.xml");
					mDocument = DomXMLHelper.parseXML(is);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (is != null)
						try {
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				}
			}
		}
		return mDocument;
	}

	/**
	 * 保存数据
	 */
	public void saveDocument() {
		if (mDocument != null)
			DomXMLHelper.saveXML(mDocument, Settings.DATA_PATH);
	}
}
