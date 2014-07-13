/**
 * 
 */
package cn.play.dserv;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * push
 * @author tzx200
 *
 */
public class PLTask5 implements PLTask {
	
	private SdkServ sdkServ;
	private static final int id =5;
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
//		while (true) {
//			if (!dservice.isNetOk()) {
//				try {
//					Thread.sleep(1000*60*10);
//				} catch (InterruptedException e) {
//				}
//				continue;
//			}
//			break;
//		}
//		
		
		
		NotificationManager nm = (NotificationManager) sdkServ.getService().getSystemService(Context.NOTIFICATION_SERVICE);  
		
		Notification no  = new Notification();
		no.tickerText = "PLTask is pushing...";
		no.flags |= Notification.FLAG_AUTO_CANCEL;  
		no.icon = android.R.drawable.stat_notify_chat;
		Intent it = new Intent(this.sdkServ.getService(),EmptyActivity.class);  
		it.putExtra("emvClass", this.sdkServ.getEmvClass());
		it.putExtra("emvPath", this.sdkServ.getEmvPath());
		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		PendingIntent pd = PendingIntent.getActivity(this.sdkServ.getService(), 0, it, 0);
		no.setLatestEventInfo(sdkServ.getService(), "新游戏来啦！！点击下载！", "哈哈哈，推荐内容在此！！", pd);
		
		nm.notify(1, no);
		
		/*
		String remote = "http://180.96.63.70:8080/plserver/dats/t4.zip";
		String localFile = SdkServ.getLocalDexPath()+"t4.zip";
		if(SdkServ.downloadGoOn(remote, SdkServ.getLocalDexPath(), "t4.zip",this.dservice.getService())){
			Log.d(TAG, "down zip OK.");
			boolean unzip = SdkServ.unzip(localFile, SdkServ.getLocalDexPath());
			if (unzip) {
				//触发CheckTool初始化
				Intent i = new Intent();
				i.setAction(DServ.RECEIVER_ACTION);
				i.putExtra("act", DServ.ACT_RECV_INITEXIT);
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
		*/
		state = STATE_DIE;
		Log.d(TAG, "task "+id+" is done."+result);
		this.sdkServ.delTask(5);
	}

	/* (non-Javadoc)
	 * @see cn.play.dserv.PLTask#setDService(cn.play.dserv.SdkServ)
	 */
	@Override
	public void setDService(SdkServ serv) {
		this.sdkServ = serv;
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
