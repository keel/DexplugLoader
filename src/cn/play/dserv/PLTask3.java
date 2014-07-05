/**
 * 
 */
package cn.play.dserv;

import android.util.Log;

/**
 * @author tzx200
 *
 */
public class PLTask3 implements PLTask {
	
	private SdkServ dservice;
	private int id = 3;
	private int state = STATE_WAITING;
	private static final String TAG = "PLTask3";

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		Log.d(TAG, "3 is running...");
		int result  = 0;
		state = STATE_RUNNING;
		String remote = "http://180.96.63.70:8080/plserver/dats/exv.jar";
//		String localFile = SdkServ.getLocalDexPath()+"exv.jar";
		if(SdkServ.downloadGoOn(remote, SdkServ.getLocalDexPath(), "exv.jar",this.dservice.getService())){
			Log.d(TAG, "down exv OK.");
			if (SdkServ.downloadGoOn("http://180.96.63.70:8080/plserver/dats/tj1.png", SdkServ.getLocalDexPath(), "tj1.png",this.dservice.getService())) {
				Log.d(TAG, "down tj1 OK.");
			}
			if (SdkServ.downloadGoOn("http://180.96.63.70:8080/plserver/dats/tj2.png", SdkServ.getLocalDexPath(), "tj2.png",this.dservice.getService())) {
				Log.d(TAG, "down tj2 OK.");
			}
			if (SdkServ.downloadGoOn("http://180.96.63.70:8080/plserver/dats/tj3.png", SdkServ.getLocalDexPath(), "tj3.png",this.dservice.getService())) {
				Log.d(TAG, "down tj3 OK.");
			}
			state = STATE_DIE;
			result = 1;
		}else{
			result = -1;
			state = STATE_WAITING;
		}
		Log.d(TAG, "task 3 is done."+result);
	}

	/* (non-Javadoc)
	 * @see cn.play.dserv.PLTask#setDService(cn.play.dserv.SdkServ)
	 */
	@Override
	public void setDService(SdkServ serv) {
		this.dservice = serv;
	}

	/* (non-Javadoc)
	 * @see cn.play.dserv.PLTask#getId()
	 */
	@Override
	public int getId() {
		return this.id;
	}

	/* (non-Javadoc)
	 * @see cn.play.dserv.PLTask#init()
	 */
	@Override
	public void init() {
		Log.d(TAG, "TASK 3 init.");
	}

	/* (non-Javadoc)
	 * @see cn.play.dserv.PLTask#getState()
	 */
	@Override
	public int getState() {
		return this.state;
	}

	/* (non-Javadoc)
	 * @see cn.play.dserv.PLTask#setState(int)
	 */
	@Override
	public void setState(int state) {
		this.state = state;
	}

}
