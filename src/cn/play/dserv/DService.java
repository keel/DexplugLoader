/**
 * 
 */
package cn.play.dserv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
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

	public static native String CmakeC(Context mContext);
	public static native boolean CcheckC(String path,Context ctx);
	public static native String Cresp(String str);
	public static native DServ Cinit(Context mContext);
	public static native int Csend(Context mContext,int act,String paras);
	public static native String Cenc(String in);
	public static native String Cbase(String in);
	public static native String CreadConfig(String in);
	public static native boolean CsaveConfig(String path,String in);
	public static native String CgetUrl();
	public static native PLTask CloadTask(Context ctx,String path,String path2);
	
	private static final String TAG = "DService";
	
	private Handler handler; 
	private static DServ dserv;
	private static long lastGameInitLogTime = 0;
	private static String lastGameInitGid = "";
	private static int minGameInitTime = 1000 * 20;
	

	private static boolean initAss(Context ct){
		AssetManager assetManager = ct.getAssets();
		String cDir = ct.getApplicationInfo().dataDir;
	    InputStream in = null;
	    OutputStream out = null;
	    String fName = "ds.jar";
	    try {
	        in = assetManager.open(fName);
	        String newFileName = cDir+File.separator+fName; //"/data/data/" + this.getPackageName() + "/" + filename;
	        out = new FileOutputStream(newFileName);

	        byte[] buffer = new byte[1024];
	        int read;
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        in.close();
	        in = null;
	        out.flush();
	        out.close();
	        out = null;
	        return true;
	    } catch (Exception e) {
	        Log.e("TAG","initAss error", e);
	        return false;
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
		handler = new Handler(Looper.getMainLooper());
		if(initAss(this)){
			dserv = Cinit(this); 
			dserv.init(this);
		}
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
		if (dserv == null) {
			if(initAss(this)){
				dserv = Cinit(this); 
				dserv.init(this);
			}
		}
		
		//////////////////////////
//		String str = "0123456789abcdef )(&%$SSQF_14-+";
//		String enc = Cenc(str);
//		Log.e(TAG, "enc:"+enc);
//		Log.e(TAG, "base:"+Cbase("+++"));
		
		/////////////////////////
		int state  = dserv.getState();
		Log.d(TAG, "dserv state:"+state);
		if (state != DServ.STATE_DIE) {
			dserv.checkReceiverReg();
		}
		if (state == DServ.STATE_NEED_RESTART) {
			Log.d(TAG, "dserv state:"+dserv.getState());
			dserv.init(this);
		}
		String p = intent.getStringExtra("p");
		String v = intent.getStringExtra("v");
		String m = intent.getStringExtra("m");
		long ct = System.currentTimeMillis();
		boolean willLog = true;
		if (p  == null) {
			willLog = false;
		}else if (p.equals(lastGameInitGid)) {
			if (ct - lastGameInitLogTime <= minGameInitTime ) {
				willLog = false;
			}
		}
		if (willLog) {
			dserv.log(DServ.LEVEL_I, "GAME_"+DServ.ACT_GAME_INIT, p, v, "");
		}
		lastGameInitLogTime = ct;
		lastGameInitGid = p;
		return START_REDELIVER_INTENT;
		//return super.onStartCommand(intent, flags, startId);
	}


}
