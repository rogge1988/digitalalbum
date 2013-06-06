package com.rogge.components;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import android.text.TextUtils;

/**
 * 使用Dom解析xml文件
 * @author Rogge
 * @date 2013-5-26
 */
public class DomXMLHelper {
	/**
	 * DOM读XML
	 */
	public static Document parseXML(InputStream inStream) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(inStream);
			return dom;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * DOM读XML
	 */
	public static Document parseXML(File file) {
		if (file == null || !file.exists())
			return null;
		Document dom = null;
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			dom = parseXML(is);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return dom;
	}

	/**
	 * DOM读XML
	 */
	public static Document parseXML(String path) {
		if (TextUtils.isEmpty(path))
			return null;
		Document dom = parseXML(new File(path));
		return dom;
	}

	/**
	 * 保存xml
	 */
	public static void saveXML(Node document, String path) {
		// make output file
		File file = new File(path);
		if (!file.exists()) {
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// transfactory
		TransformerFactory transFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		DOMSource domSource = new DOMSource(document);
		StreamResult xmlResult = new StreamResult(out);
		try {
			transformer.transform(domSource, xmlResult);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
}