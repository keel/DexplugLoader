/**
 * 
 */
package com.k99k.tools.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

/**
 * IO操作
 * @author keel
 *
 */
public final class IO {

	public IO() {
	}

	private static final String TAG = "IO";
	/**
	 * 读取raw下的文件
	 * @param context
	 * @param file raw下的文件
	 * @return
	 */
	public static final String readRaw(Context context, int resId) {
		String data = "";
		try {
			//InputStream stream = context.getAssets().open(file);
			//InputStream stream = context.getResources().openRawResource(resId);
			BufferedReader in = new BufferedReader(
		            new InputStreamReader(context.getResources().openRawResource(resId), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			int c;
			while ((c = in.read()) != -1) {
				sb.append((char) c);
			}
			in.close();
			if (sb.length() > 0) {
				data = sb.toString();
			}

		} catch (FileNotFoundException e) {
			Log.e(TAG, "readIndex - FileNotFound:" + resId,e);
			return "";
		} catch (IOException e) {
			Log.e(TAG, "readIndex - File read error:" + resId,e);
			return "";
		}
		return data;
	} 
	
	/**
	 * 读取文本文件
	 * @param context
	 * @param file
	 * @return 失败则返回空字符串
	 */
	public static final String readTxt(String file) { 
		String data = "";
		try {
			BufferedReader in = new BufferedReader(
		            new InputStreamReader(new FileInputStream(file), "UTF-8"));
			StringBuffer sb = new StringBuffer();
			int c;
			while ((c = in.read()) != -1) {
				sb.append((char) c);
			}
			in.close();
			data = sb.toString();

		} catch (FileNotFoundException e) {
			return "";
		} catch (IOException e) {
			Log.e(TAG, "File read error:" + file,e);
			return "";
		}
		return data;
	} 
	

	
	/**
	 * 写入文件到本地files目录,utf-8方式
	 * @param context Context
	 * @param file 本地文件名
	 * @param msg 需要写入的字符串
	 * @param isAppend 是否追加
	 */
	public static final void writeTxt(String file, String msg,boolean isAppend) {
		try {
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file,isAppend),"utf-8"));

//			FileOutputStream stream = context.openFileOutput(file, Context.MODE_PRIVATE);
//			stream.write(msg.getBytes());
//			stream.flush();
			out.write(msg);
			out.close();
			
		} catch (IOException e) {
			Log.e(TAG, "writeTxt error:" + file,e);
			return;
		}
	}
	
	public static final void writeTxt(String file, String msg) {
		writeTxt(file, msg, false);
	}
	
	/**
	 * 保存图片到SD卡,如果SD卡不存在,直接保存到内存
	 * @param fileName 文件名
	 * @param savePath 文件路径,带/sdcard的路径
	 * @param pic 图片
	 * @return 是否成功保存
	 */
	public static final boolean savePic(Context context,String fileName,String savePath,Bitmap pic){
		if (pic == null) {
			return false;
		}
		try {

			//如果SD卡未挂载
			boolean sdcardIn = true;
			
			File sd = android.os.Environment.getExternalStorageDirectory();
			
//			if (android.os.Environment.getExternalStorageState() != android.os.Environment.MEDIA_MOUNTED) {
//				sdcardIn = false;
//			}
//			Log.e(TAG, "sdcardIn:"+sdcardIn);
			//File sd = new File("/sdcard");
			if (!sd.canWrite()) {
				sdcardIn = false;
			}
			if (!sdcardIn) {
				//FIXME 无SD卡则直接提示无法保存
				//savePath = savePath.substring(savePath.indexOf("sdcard")+6);
				return false;
			}
			//FileOutputStream out = this.openFileOutput(fileName, MODE_WORLD_READABLE);//这是保存到/data/package下面，不能定义位置
			//File f = new File(savePath);
			//if (!f.exists()) {
			int isS = savePath.indexOf("sdcard");
			if (isS>=0) {
				savePath = sd.getPath()+savePath.substring(isS+6);
			}
			
			//Log.e(TAG, "savePath:"+savePath);
			(new File(savePath)).mkdirs();
			//}
			//File file = new File(savePath+fileName);
			//Log.e(TAG, "canWrite:"+sdcardIn+" savePath:"+savePath +" fileName:"+fileName+" file.canWrite:"+file.canWrite());
			FileOutputStream out = new FileOutputStream(savePath+fileName);
			pic.compress(CompressFormat.JPEG, 95, out);
			out.flush();
			out.close();
			//扫描到媒体库
			//if (mediaScanConn == null) {
			mediaScanConn = new MediaScannerConnection(context,new MediaSannerClient(savePath+fileName,"image/jpeg"));  
			//}
			mediaScanConn.connect();
			return true;
		} catch (IOException e) {
			Log.e(TAG, "savePic error:" + fileName,e);
			return false;
		}

	}
	
	/**
	 * 用于扫描wallpaper目录下图片的扫描器
	 */
	static MediaScannerConnection mediaScanConn = null;//new MediaScannerConnection(this,new MusicSannerClient());  
	
	/**
	 * 媒体扫描器
	 * @author keel
	 *
	 */
	static class MediaSannerClient implements MediaScannerConnection.MediaScannerConnectionClient {

		public MediaSannerClient(String filePath,String fileType){
  			this.filePath = filePath;
  			this.fileType = fileType;
  		}
  
  		public MediaSannerClient(String[] filePaths,String fileType){
  			this.filePaths = filePaths;
  			this.fileType = fileType;
  		}
  		String filePath = null;
  		String fileType = null;
  		String[] filePaths = null;
  		
		public void onMediaScannerConnected() {
			
			if (filePath != null) {
				mediaScanConn.scanFile(filePath, fileType);
			}

			if (filePaths != null) {
				for (String file : filePaths) {
					mediaScanConn.scanFile(file, fileType);
				}
			}
		}

		public void onScanCompleted(String path, Uri uri) {
			Log.i(TAG, "scan media complet:"+path+" uri:"+uri.toString());
			mediaScanConn.disconnect();
		}
		
	}
	
	public static final Bitmap resizePic(Bitmap b,int maxHeight){
		int orgWidth = b.getWidth();
		int orgHeight = b.getHeight();
//		Log.d(TAG, "maxHeight:"+maxHeight+" orgHeight:"+orgHeight);
		float scale = ((float)maxHeight)/((float)orgHeight);
//		float toWidth = (orgWidth*scale);
//		float toHeight = (orgHeight*scale);
//		Log.d(TAG, "scale:"+scale);
//		Log.d(TAG, "toWidth:"+toWidth);
//		Log.d(TAG, "toHeight:"+toHeight);
		
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap newBmp = Bitmap.createBitmap(b,0,0,orgWidth,orgHeight,matrix,true);
//		Log.d(TAG, "newHeight:"+newBmp.getHeight());
//		Log.d(TAG, "newWidth:"+newBmp.getWidth());
		return newBmp;
	}
	
	//-------------------配置读取与写入
	
	private static SharedPreferences settings;
	private static Editor editor;
	
	final static String PREFS_NAME = "k99k_APP";  
	
	public static final SharedPreferences getSharedPreferences(Context context){
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}
	
	public static final String getPropString(Context context,String key,String defValue){
		if (settings == null) {
			settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		}
		try {
			return settings.getString(key, defValue);
		} catch (Exception e) {
			return defValue;
		}
	}
	
	public static final int getPropInt(Context context,String key,int defValue){
		if (settings == null) {
			settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		}
		try {
			return settings.getInt(key, defValue);
		} catch (Exception e) {
			return defValue;
		}
	}

	public static final boolean getPropBoolean(Context context,String key,boolean defValue){
		if (settings == null) {
			settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		}
		try {
			return settings.getBoolean(key, defValue);
		} catch (Exception e) {
			return defValue;
		}
	}
	
	public static final void setPropString(Context context,String key,String value){
		if (settings == null || editor == null) {
			settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
			editor = settings.edit();
		}
		editor.putString(key,value);   
		editor.commit();
	}
	
	public static final void setPropInt(Context context,String key,int value){
		if (settings == null || editor == null) {
			settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
			editor = settings.edit();
		}
		editor.putInt(key,value);   
		editor.commit();
	}
	
	public static final void setPropBoolean(Context context,String key,boolean value){
		if (settings == null || editor == null) {
			settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
			editor = settings.edit();
		}
		editor.putBoolean(key,value);   
		editor.commit();
	}
	
	public static final void setProps(Context context,String[] key,Object[] value){
		if (settings == null || editor == null) {
			settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
			editor = settings.edit();
		}
		for (int i = 0; i < key.length; i++) {
			if (value[i] instanceof String) {
				editor.putString(key[i], (String) value[i]);
			}else if(value[i] instanceof Integer){
				editor.putInt(key[i], Integer.valueOf(value[i].toString()));
			}else if(value[i] instanceof Boolean){
				editor.putBoolean(key[i], Boolean.valueOf(value[i].toString()));
			}
		}  
		editor.commit();
	}
	
}
