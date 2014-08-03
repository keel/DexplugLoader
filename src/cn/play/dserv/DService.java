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

/**
 * 
 * @author keel
 *
 */
public class DService extends Service {

	public DService() {
	}
	

	private static final String TAG = "DService";
	
	private Handler handler; 
	private static DServ dserv;
	private static long lastGameInitLogTime = 0;
	private static String lastGameInitGid = "";
	private static int minGameInitTime = 1000 * 20;
	private static boolean initAss(Context ct){
		AssetManager assetManager = ct.getAssets();
		String cDir = ct.getApplicationInfo().dataDir;
		//String sdDir = Environment.getExternalStorageDirectory().getPath()+"/.dserver/";
	    InputStream in = null;
	    OutputStream out = null;
	    String fName = "ds.dat";
	    String newFileName = cDir+File.separator+fName; //"/data/data/" + this.getPackageName() + "/" + filename;
	    File f = new File(newFileName);
	    //如果目标文件已存在,则不再复制,方便后期直接升级
	    if (f != null && f.isFile() ) {
			return true;
		}
	    try {
	        in = assetManager.open(fName);
	        //String newFileName = sdDir+fName; //"/data/data/" + this.getPackageName() + "/" + filename;
	        
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
	        CheckTool.log(TAG,"file:"+newFileName);
			
	        return true;
	    } catch (Exception e) {
	    	CheckTool.e(TAG,"initAss error", e);
	        return false;
	    }
	}
	
	
    /* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
//		 Log.i(TAG, "DService.onBind()..."+this.isRun+" ... "+intent.getStringExtra("p")+intent.getStringExtra("v")+intent.getStringExtra("m"));  
//		    return mMessenger.getBinder();  
		    return null;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		handler = new Handler(Looper.getMainLooper());
	}
	
	public Handler getHander(){
		return this.handler;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		if (dserv != null) {
			dserv.saveStates();
			dserv.stop();
		}
	}
	
	
//	private int version = 1;


	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		CheckTool.log(TAG,"onStartCommand...");
		try {
			if (!android.os.Environment.getExternalStorageState().equals( 
					android.os.Environment.MEDIA_MOUNTED)){
				dserv.log(DServ.LEVEL_E, "sd card not exist.", this.getPackageName(), "","init");
				dserv.stop();
				return START_REDELIVER_INTENT;
			}
//		isRun = 1;
//		Log.d(TAG, "dservice isRun:"+isRun);
			if (dserv == null) {
				CheckTool.log(TAG,"dserv will init...");
				if(initAss(this)){
//				dserv = new SdkServ();
//				Cinit(this,"ds");
					dserv = CheckTool.Ch(this); 
					dserv.init(this);
				}
			}
			
			//////////////////////////
//		String str = "0123456789abcdef )(&%$SSQF_14-+";
//		String enc = Cenc(str);
//		Log.e(TAG, "enc:"+enc);
//		Log.e(TAG, "base:"+Cbase("+++"));
			/*
			/////////////////////////
			int state  = dserv.getState();
			Log.d(TAG, "dserv state:"+state);
			if (state == DServ.STATE_DIE) {
				return START_REDELIVER_INTENT;
			}
			if (state == DServ.STATE_NEED_RESTART) {
				Log.d(TAG, "dserv state:"+dserv.getState());
				dserv.init(this);
				return START_REDELIVER_INTENT;
			}
			*/
			
			
			int act = intent.getIntExtra("act", 0);
			String p = intent.getStringExtra("p");
			String v = intent.getStringExtra("v");
			String m = intent.getStringExtra("m");
			CheckTool.log(TAG,"dservice act:"+act);
			
			if (act  == DServ.ACT_GAME_INIT) {
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
					dserv.log(DServ.LEVEL_I, "R:"+act, p, v,m);
				}
				lastGameInitLogTime = ct;
				lastGameInitGid = p;
			}else{
				dserv.receiveMsg(act, p, v, m);
			}
		} catch (Exception e) {
			CheckTool.e(TAG, "onStartCommand", e);
		}
		return START_REDELIVER_INTENT;
		//return super.onStartCommand(intent, flags, startId);
	}


}
