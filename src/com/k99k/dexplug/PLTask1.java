package com.k99k.dexplug;

import android.util.Log;
import android.widget.Toast;

public class PLTask1 implements PLTask {

	public PLTask1() {
	}
	
	private DService dservice;
	private int id = 1;
	private int state = STATE_WAITING;
	private int sleepTime = 1000*5;
	private long nextRunTime = 0;
	private static final String TAG = "PLTask1";
	private int result = -1;
	
	private int runTimes = 6;

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
		Log.d(TAG, "PLTask1 inited.");
	}
	
	

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if (runTimes<=0) {
			state = STATE_DIE;
			Log.d(TAG, "1 is dead.");
			return;
		}
		Log.d(TAG, "1 is running..."+runTimes);
		state = STATE_RUNNING;
		dservice.getHander().post(new Runnable() {     
            @Override     
            public void run() {     
                   Toast.makeText(dservice.getApplicationContext(), "PLTask1 run! time:"+runTimes,Toast.LENGTH_SHORT).show(); 
                   runTimes--;
            }     
		});
		
		Log.d(TAG, "toast end.");
		//通过state控制任务是否为一次性或循环任务
		
		nextRunTime = System.currentTimeMillis() + this.sleepTime;
		state = STATE_WAITING;
		result = 0;
	}


	@Override
	public int getState() {
		if (this.state == STATE_RUNNING || this.state == STATE_DIE) {
			return this.state;
		}
		if (System.currentTimeMillis()>this.nextRunTime) {
			this.state = STATE_WAITING;
		}else{
			this.state = STATE_PAUSE;
		}
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
		return true;
	}


}
