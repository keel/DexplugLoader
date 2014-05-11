package com.k99k.dexplug;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ItemData {

	public ItemData() {
	}

	private int id;
	
	private String appName;
	
	private String subInfo;
	
	private String info;
	
	private String iconUrl;
	
	private String downUrl;

	/**
	 * @return the id
	 */
	public final int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public final void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the appName
	 */
	public final String getAppName() {
		return appName;
	}

	/**
	 * @param appName the appName to set
	 */
	public final void setAppName(String appName) {
		this.appName = appName;
	}

	/**
	 * @return the subInfo
	 */
	public final String getSubInfo() {
		return subInfo;
	}

	/**
	 * @param subInfo the subInfo to set
	 */
	public final void setSubInfo(String subInfo) {
		this.subInfo = subInfo;
	}

	/**
	 * @return the info
	 */
	public final String getInfo() {
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public final void setInfo(String info) {
		this.info = info;
	}


	/**
	 * @return the icon
	 */
	public final Bitmap getIcon() {
		Bitmap b = BitmapFactory.decodeFile(this.iconUrl);
		return b;
	}

	/**
	 * @return the downUrl
	 */
	public final String getDownUrl() {
		return downUrl;
	}

	/**
	 * @param downUrl the downUrl to set
	 */
	public final void setDownUrl(String downUrl) {
		this.downUrl = downUrl;
	}

	/**
	 * @return the iconUrl
	 */
	public final String getIconUrl() {
		return iconUrl;
	}

	/**
	 * @param iconUrl the iconUrl to set
	 */
	public final void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}
	
	
	
}
