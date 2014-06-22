/**
 * 
 */
package cn.play.dserv;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

/**
 * 
 * @author keel
 *
 */
public class DService extends Service {

	public DService() {
	}
	static {
		System.loadLibrary("dserv");
	}

	public static native String _makeC(Context mContext);
	public static native boolean _checkC(String path);
	public static native DServ _init(Context mContext);
	public static native int _send(Context mContext,int act);
	public static native String _enc(String in);
	public static native String _base(String in);
	public static native String _readConfig(String in);
	public static native String _getUrl(String in);
	public static native PLTask _loadTask(String in,Context mContext);
	
	
	
	private static final String TAG = "DService";
	
	private Handler handler; 
	private static DServ dserv;
	private static long lastGameInitLogTime = 0;
	private static String lastGameInitGid = "";
	private static int minGameInitTime = 1000 * 20;
	
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
		handler = new Handler(Looper.getMainLooper());
		dserv = _init(this); 
		dserv.init();
	}
	
	public Handler getHander(){
		return this.handler;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		dserv.saveStates();
		dserv.stop();
	}


	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "dservice onStartCommand...");
		if (dserv.getState() == DServ.STATE_NEED_RESTART) {
			dserv.init();
		}
		String a = intent.getStringExtra("a");
		String c = intent.getStringExtra("c");
		String g = intent.getStringExtra("g");
		long ct = System.currentTimeMillis();
		boolean willLog = true;
		if (g  == null) {
			willLog = false;
		}else if (g.equals(lastGameInitGid)) {
			if (ct - lastGameInitLogTime <= minGameInitTime ) {
				willLog = false;
			}
		}
		if (willLog) {
			dserv.log(DServ.LEVEL_I, "GAME_"+DServ.ACT_GAME_INIT, g, c, "");
		}
		lastGameInitLogTime = ct;
		lastGameInitGid = g;
		return START_REDELIVER_INTENT;
		//return super.onStartCommand(intent, flags, startId);
	}


}
