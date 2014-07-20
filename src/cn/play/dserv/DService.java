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
	public static native DServ Cinit(Context mContext,String dat);
	public static native int Csend(Context mContext,int act,String vals,String msg);
	public static native int Csendb(Context mContext,int act,String vals,String msg);
	public static native String Cenc(String in);
	public static native String Cbase(String in);
	public static native String CreadConfig(String in);
	public static native boolean CsaveConfig(String path,String in);
	public static native String CgetUrl();
	public static native PLTask CloadTask(Context ctx,int id,String className);
	public static native Object Cload(String path,String className,Context ctx,boolean initWithContext);
	
	private static final String TAG = "DService";
	
	private Handler handler; 
	private static DServ dserv;
	private static long lastGameInitLogTime = 0;
	private static String lastGameInitGid = "";
	private static int minGameInitTime = 1000 * 20;
	/*
	private int isRun = 0;
	
//	private int testVal = 100;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			if (DService.this.isRun == 0) {
				try {
					msg.replyTo.send(Message.obtain(null, 0, 0, 0));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				return;
			}else{
				try {
					msg.replyTo.send(Message.obtain(null, 0, 1, 1));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			
			
			int w = msg.what;
			Bundle data = msg.getData();
			String p = data.getString("p");
			String v = data.getString("v");
			String m = data.getString("m");
			
			Log.e(TAG, "======== Got MSG ======== ["+w+"] p:"+p+" v:"+v+" m:"+m);
			if (w  == DServ.ACT_GAME_INIT) {
				long ct = System.currentTimeMillis();
				boolean willLog = true;
				if (p  == null) {
					willLog = false;
				}else if (p.equals(lastGameInitGid)) {
					if (ct - lastGameInitLogTime <= minGameInitTime ) {
						willLog = false;
					}
				}
				lastGameInitLogTime = ct;
				lastGameInitGid = p;
				if (willLog) {
					dserv.receiveMsg(w, p, v, m);
				}
			}else{
				dserv.receiveMsg(w, p, v, m);
			}
			
			
//			switch (w) {
//			case 123:
//				Log.e(TAG, "Get Message from MainActivity.");
//				break;
//			case 91:
//				Log.e(TAG, "MSG_REGISTER_CLIENT.");
//				break;
//			case 92:
//				Log.e(TAG, "MSG_UNREGISTER_CLIENT.");
//				break;
//			case 93:
//				int value = msg.arg1;
//				testVal = testVal+value;
//				Log.e(TAG, "MSG_SET_VALUE."+value+" testVal:"+testVal);
//				try {
//					msg.replyTo.send(Message.obtain(null, 93, testVal,
//							0));
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				}
//				break;
//			default:
//				super.handleMessage(msg);
//			}
		}
	};
	//It's the messenger of server  
	private Messenger mMessenger = new Messenger(mHandler);  
*/
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
	        Log.d(TAG, "file:"+newFileName);
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
		Log.d(TAG, "on create.................");
	}
	
	@Override
	public void onRebind(Intent intent) {
		Log.e(TAG, "onRebind........");
		super.onRebind(intent);
	}
	@Override
	public boolean onUnbind(Intent intent) {
		Log.e(TAG, "onUnbind........");
		
		return super.onUnbind(intent);
	}
	public Handler getHander(){
		return this.handler;
	}

	/* (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Log.e(TAG, "onDestroy........");
		if (dserv != null) {
			dserv.saveStates();
			dserv.stop();
		}
	}
	
	
	private int version = 1;


	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "dservice onStartCommand...");
		if (!android.os.Environment.getExternalStorageState().equals( 
				android.os.Environment.MEDIA_MOUNTED)){
			dserv.log(DServ.LEVEL_E, "sd card not exist.", this.getPackageName(), "","init");
			dserv.stop();
			return START_REDELIVER_INTENT;
		}
//		isRun = 1;
//		Log.d(TAG, "dservice isRun:"+isRun);
		if (dserv == null) {
			Log.e(TAG, "dserv will init");
			if(initAss(this)){
				//FIXME 测试用
//				dserv = new SdkServ();
//				Cinit(this,"ds");
				dserv = Cinit(this,"ds"); 
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
		return START_REDELIVER_INTENT;
		//return super.onStartCommand(intent, flags, startId);
	}


}
