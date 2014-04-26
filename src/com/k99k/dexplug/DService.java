/**
 * 
 */
package com.k99k.dexplug;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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
	
	public static final int TYPE_UPDATE = 0;
	public static final int TYPE_ADD_TASK = 1;
	public static final int TYPE_DEL_TASK = 2;
	public static final int TYPE_STOP_SERVICE = 3;
	public static final int TYPE_RESTART_SERVICE = 4;
	public static final int TYPE_OTHER = 5;
	
	
	private byte[] key;
	
	
	private void up(){
		
	}
	
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

	class TaskThread extends Thread{
	
		private ArrayList<PLTask> taskList = new ArrayList<PLTask>();
		
		@Override
		public void run() {
			super.run();
		}
		
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
		// TODO Auto-generated method stub
		super.onCreate();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}
	
}
