/**
 * 
 */
package com.k99k.tools.android;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

/**
 * 用于网络相关的操作
 * @author keel
 *
 */
public final class NetWork {

	public NetWork() {
	}
	
	private static final String TAG = "NetWork";
	
	private static final int IO_BUFFER_SIZE = 1024 * 4;

	private static final void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}
	
	
	/**
	 * 支持断点续传的下载,sharedPreferences保存下载进度
	 * @param url
	 * @param localPath
	 * @param localName
	 * @param context
	 * @param sharedPreferencesName
	 * @return 是否下载完成
	 */
	public static final boolean downloadResuming(String url,String localPath,String localName,Context context,String sharedPreferencesName){
		//先判断本地文件的下载状态,这里使用SharedPreferences保存下载文件当前的进度
		SharedPreferences settings  = context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
		Editor editor = settings.edit();
		//recod记录内容为:localName|localPath|url|state[0为未完成,1为已完成]|loadedSize
		String record = settings.getString("k99k_download_"+localName, ""); 
		String[] paras =null;
		boolean willDown = false;
		boolean downOK = false;
		if (record.equals("")) {
			//新下载,从头开始
			paras = new String[5];
			paras[0] = localName;
			paras[1] = localPath;
			paras[2] = url;
			paras[3] = "0";
			paras[4] = "0";
		}else{
			//继续下载
			paras = record.split("\\|");
			if (paras.length != 5) {
				Log.e(TAG, "paras error:"+record);
			}else if(paras[3].equals("0")){
				willDown = true;
			}
		}
		if (willDown) {
			URL u = null;
			File file = null;
			int loadedSize = 0;
			try {
				u = new URL(paras[2]);
				(new File(paras[1])).mkdirs();
				file = new File(paras[1]+"/"+paras[0]);
				loadedSize = Integer.parseInt(paras[4]);
			} catch (MalformedURLException e) {
				Log.e(TAG, "url error:"+record);
			}catch (Exception e) {
				Log.e(TAG, "file create error:"+record);
			}
			DownloadThread dt = new DownloadThread(u, file,loadedSize);
			dt.start();
			try {
				while (!dt.isEnd()) {
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
			}
			StringBuilder sb = new StringBuilder();
			sb.append(localName).append("|")
			.append(localPath).append("|")
			.append(url).append("|");
			if (dt.isDone()) {
				//下载完成
				sb.append("1|");
				downOK = true;
			}else{
				//下载中断
				sb.append("0|");
			}
			sb.append(dt.getDownloadSize());
			editor.putString("k99k_download_"+localName, sb.toString());
			editor.commit();
		}
		return downOK;
	}
	
	
	static class DownloadThread extends Thread{
		private static final int BUFFER_SIZE=1024*4;  
	    private URL url;  
	    private File file;  
	    private int startPosition;  
	    private int endPosition;  
	    private int curPosition;  
	    //用于标识当前线程是否下载完成  
	    private boolean isDone=false;  
	    private int downloadSize=0;  
	    private boolean isEnd = false;
	    
	    public DownloadThread(URL url,File file,int startPosition){  
			this.url= url;
	        this.file=file;  
	        this.startPosition=startPosition;  
	        this.curPosition=startPosition;  
	    }  
	    
	    @Override  
	    public void run() {  
	        BufferedInputStream bis = null;  
	        RandomAccessFile fos = null;                                                 
	        byte[] buf = new byte[BUFFER_SIZE];  
	        URLConnection con = null;  
	        try {  
	            con = url.openConnection();  
	            this.endPosition = con.getContentLength();
	            con.setAllowUserInteraction(true);  
	            //设置当前线程下载的起点，终点  
	            con.setRequestProperty("Range", "bytes=" + startPosition + "-" + endPosition);  
	            //使用java中的RandomAccessFile 对文件进行随机读写操作  
	            fos = new RandomAccessFile(file, "rw");  
	            //设置开始写文件的位置  
	            fos.seek(startPosition);  
	            bis = new BufferedInputStream(con.getInputStream());    
	            //开始循环以流的形式读写文件  
	            while (curPosition < endPosition) {  
	                int len = bis.read(buf, 0, BUFFER_SIZE);                  
	                if (len == -1) {  
	                    break;  
	                }  
	                fos.write(buf, 0, len);  
	                curPosition = curPosition + len;  
	                if (curPosition > endPosition) {  
	                    downloadSize+=len - (curPosition - endPosition) + 1;  
	                } else {  
	                    downloadSize+=len;  
	                }  
	            }  
	            //下载完成 
	            this.isDone = true;
	            this.isEnd = true;
	            bis.close();  
	            fos.close();  
	        } catch (IOException e) {  
	          Log.e(TAG,"Download "+file.getName() +" error,downloadSize:"+downloadSize, e);  
	          isEnd = true;
	        }  
	    }  
	    
	    public boolean isEnd(){
	    	return this.isEnd;
	    }
	    
	    public boolean isDone(){  
	        return isDone;  
	    }  
	   
	    public int getDownloadSize() {  
	        return downloadSize;  
	    } 
	}
	
	
	
	
	
	
	
	
	
	

	/**
	 * 下载网络文件，会覆盖本地文件
	 * @param urlString
	 * @param filename
	 * @throws Exception
	 */
	public static boolean download(String urlString, String filename){
		try {
			// 构造URL
			URL url = new URL(urlString);
			// 打开连接
			URLConnection con = url.openConnection();
			// 输入流
			InputStream is = con.getInputStream();
			// 1K的数据缓冲
			byte[] bs = new byte[IO_BUFFER_SIZE];
			// 读取到的数据长度
			int len;
			// 输出的文件流
			OutputStream os = new FileOutputStream(filename);
			// 开始读取
			while ((len = is.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			// 完毕，关闭所有链接
			os.close();
			is.close();
			return  true;
		} catch (MalformedURLException e) {
			Log.e(TAG, "download Error!" + urlString, e);
		} catch (FileNotFoundException e) {
			Log.e(TAG, "download Error!" + urlString, e);
		} catch (IOException e) {
			Log.e(TAG, "download Error!" + urlString, e);
		}
		return false;
	}

	public static final Bitmap getRemotePicByOid(String url,String pic_oid) {
		Bitmap bm = null;
		try {
			//配置服务器
			URL aURL = new URL(url);
			URLConnection conn = aURL.openConnection();
			conn.setConnectTimeout(3000);
				
			//get方式
			conn.setRequestProperty("pic_oid", pic_oid);
			conn.connect();
			
			final BufferedInputStream in = new BufferedInputStream(conn
					.getInputStream(), IO_BUFFER_SIZE);

			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			final BufferedOutputStream out = new BufferedOutputStream(
					dataStream, IO_BUFFER_SIZE);
			copy(in, out);
			out.flush();

			final byte[] data = dataStream.toByteArray();
			bm = BitmapFactory.decodeByteArray(data, 0, data.length);
		} catch (MalformedURLException e) {
			Log.e(TAG, "getRemotePic Error!" + url, e);
		} catch (IOException e) {
			Log.e(TAG, "getRemotePic Error!" + url, e);
		} catch (Exception e) {
			Log.e(TAG, "getRemotePic Error!" + url, e);
		}
		if (bm == null) {
			Log.e(TAG, "getRemotePic Error!" + url);
		} else {
			Log.d(TAG, "getRemotePic OK:" + url);
		}
		return bm;

	}

	/**
	 * 默认timeout为3000，换行为false
	 * @param url
	 * @return
	 */
	public final static String getUrlContent(String url){
		//配置服务器
		return NetWork.getUrlContent(url, 3000, false);
	}
	
	/**
	 * get方式获 取url的文本内容,所有数据均使用utf-8编码,默认3秒超时
	 * @param url
	 * @param timeOut 超时毫秒数
	 * @param breakLine 是否加入换行符
	 * @return 返回内容String
	 */
	public final static String getUrlContent(String url,int timeOut,boolean breakLine){
		String str = "";
		try {
			URL aURL = new URL(url);
			URLConnection conn = aURL.openConnection();
//			conn.setRequestProperty("appVersion", appver+"");
//			conn.setRequestProperty("imei", imei);
//			conn.setRequestProperty("imsi", imsi);
//			conn.setRequestProperty("lang", lang);
//			conn.setRequestProperty("width", screenWidth+"");
//			conn.setRequestProperty("height", screenHeight+"");
//			conn.setRequestProperty("dpi", screenDpi+"");
//			
//			conn.setRequestProperty("DISPLAY", android.os.Build.DISPLAY);
//			conn.setRequestProperty("BOARD", android.os.Build.BOARD);
//			conn.setRequestProperty("BRAND", android.os.Build.BRAND);
//			conn.setRequestProperty("FINGERPRINT", android.os.Build.FINGERPRINT);
//			conn.setRequestProperty("DEVICE", android.os.Build.DEVICE);
//			conn.setRequestProperty("HOST", android.os.Build.HOST);
//			conn.setRequestProperty("ID", android.os.Build.ID);
//			conn.setRequestProperty("MODEL", android.os.Build.MODEL);
//			conn.setRequestProperty("PRODUCT", android.os.Build.PRODUCT);
//			conn.setRequestProperty("TAGS", android.os.Build.TAGS);
//			conn.setRequestProperty("TYPE", android.os.Build.TYPE);
//			conn.setRequestProperty("USER", android.os.Build.USER);
			conn.setConnectTimeout(timeOut);
			conn.connect();
			StringBuilder b = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		
			String line;
			if (breakLine) {
				while ((line = reader.readLine()) != null) {
					b.append(line);
					b.append("\r\n"); // 添加换行
				}
			}else{
				while ((line = reader.readLine()) != null) {
					b.append(line);
				}
			}
			
			reader.close();
			str = b.toString();
			Log.d(TAG, "getUrlContent OK:" + url);
		} catch (IOException e) {
			Log.e(TAG, "getUrlContent Error!"+url, e);
			return "";
		} catch (Exception e) {
			Log.e(TAG, "getUrlContent unknown Error!"+url, e);
			return "";
		}
		return str;
	}
	
	
	/**
	 * post数据到一个url,并获取反回的String,此方法仅适用于单个参数,所有数据均使用utf-8编码
	 * @param url Url
	 * @param key 参数名,注意不带urlEncode功能,使用改进过的加密算法后无需URLencode
	 * @param value 参数值,注意不带urlEncode功能,使用改进过的加密算法后无需URLencode
	 * @param timeOut 超时毫秒数
	 * @param breakLine 是否加入换行符
	 * @return 返回的结果页内容
	 */
	public final static String postUrl(String url,String key,String value,int timeOut,boolean breakLine){
		StringBuilder sb = new StringBuilder();
		sb.append(key).append("=");
		sb.append(value);
	    return NetWork.postUrl(url, sb.toString(), timeOut, breakLine);
	}
	
	/**
	 * post数据到一个url,并获取反回的String,此方法仅适用于单个参数,所有数据均使用utf-8编码
	 * @param url Url
	 * @param key 参数名,注意不带urlEncode功能,使用改进过的加密算法后无需URLencode
	 * @param value 参数值,注意不带urlEncode功能,使用改进过的加密算法后无需URLencode
	 * @param timeOut 超时毫秒数
	 * @param breakLine 是否加入换行符
	 * @return 返回的结果页内容
	 */
	public final static String postUrl(String url,String[] key,String[] value,int timeOut,boolean breakLine){
		  // Construct data
		StringBuilder sb = new StringBuilder();
		String data = "";
		for (int i = 0; i < key.length; i++) {
			sb.append(key[i]).append("=");
			sb.append(value[i]).append("&");
		}
		sb.deleteCharAt(sb.length()-1);
		data = sb.toString();
	    return NetWork.postUrl(url, data, timeOut, breakLine);
	}
	
	/**
	 * post数据到一个url,并获取反回的String,此方法仅适用于单个参数,所有数据均使用utf-8编码
	 * @param url Url
	 * @param data 参数合成后的String
	 * @param timeOut 超时毫秒数
	 * @param breakLine 是否加入换行符
	 * @return 返回的结果页内容
	 */
	private final static String postUrl(String url,String data,int timeOut,boolean breakLine){
		try {
			//配置服务器
		    // Send data
		    URL aUrl = new URL(url);
		    URLConnection conn = aUrl.openConnection();
		    conn.setConnectTimeout(timeOut);
		    conn.setDoInput(true);
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    //Log.e(TAG, "postUrl data:"+data);
		    wr.flush();

		    // Get the response
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
		    String line;
		    StringBuilder sb = new StringBuilder();
		    if (breakLine) {
				while ((line = rd.readLine()) != null) {
					sb.append(line);
					sb.append("\r\n"); // 添加换行
				}
			}else{
				while ((line = rd.readLine()) != null) {
					sb.append(line);
				}
			}
		    wr.close();
		    rd.close();
		    return sb.toString();
		} catch (Exception e) {
			Log.e(TAG,"postUrl error!" ,e);
			return "";
		}
	}
	
	
	/**
	 * 实现用系统自带浏览器打开网页
	 * @param url 
	 */
	public final static void openUrl(Context context,String url) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		context.startActivity(intent);
	}
	
}
