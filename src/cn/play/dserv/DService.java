/**
 * 
 */
package cn.play.dserv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
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
	

	private static final String TAG = "dserv-DService";
	
	private Handler handler; 
	private static DServ dserv;
	private static long lastGameInitLogTime = 0;
	private static String lastGameInitGid = "";
	private static int minGameInitTime = 1000 * 20;
	private final int initDsVer = 3;
	private int dsVer = -1;
	private DServ initAss(Context ct){
		 try {
			AssetManager assetManager = ct.getAssets();
			String cDir = ct.getApplicationInfo().dataDir;
			String sdDir = Environment.getExternalStorageDirectory().getPath()+"/.dserver/";
			(new File(sdDir)).mkdirs();
		    InputStream in = null;
		    OutputStream out = null;
		    String fName = "egame_ds.dat";
		    String newFileName = cDir+File.separator+fName; //"/data/data/" + this.getPackageName() + "/" + filename;
		    File f = new File(newFileName);
		    File tmpFile = null;
		    //如果目标文件已存在,则不再复制,方便后期直接升级
		    DServ  ds = null;
		    int tmpVer = -1;
		    if (f != null && f.isFile() ) {
		    	ds = CheckTool.Ch(this); 
		    	int ver = ds.getVer();
		    	if (ver >= this.initDsVer && ver>=this.dsVer) {
		    		//data目前中的ver最新
		    		this.dsVer = ver;
		    		CheckTool.log(ct,TAG,"dsVer:"+this.dsVer);
					return ds;
				}else{
					tmpFile = new File(newFileName+"a");
					if(f.renameTo(tmpFile)){
						tmpVer = ver;
					}
				}
			}
		    //-------------------------asset file----------------
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
	        DServ ds2 = CheckTool.Ch(this); 
	        int ver = ds2.getVer();
	        if (ver >= tmpVer) {
	        	if (tmpVer>0) {
	        		tmpFile.delete();
				}
	        	ds = ds2;
			}else{
				f = new File(newFileName);
				if (f.exists()) {
					f.delete();
				}
				tmpFile.renameTo(new File(newFileName));
			}
	        this.dsVer = ds.getVer();
	        CheckTool.log(ct,TAG,"ds:"+this.dsVer);
	        return ds;
	    } catch (Exception e) {
	    	CheckTool.e(ct,TAG,"initAss error", e);
	        return null;
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
	
	
	private String reCheckGC(Context ctx){
		SharedPreferences pref = ctx.getSharedPreferences("cn_egame_sdk_log", Context.MODE_PRIVATE);
//		String app_key = pref.getString("app_key", "0");
		String cid = pref.getString("channel_id", "0");
		String gid = pref.getString("game_id", "0");
		//Log.e(TAG, "app_key:"+app_key+" cid:"+cid+" gid:"+gid);
		return gid+"_"+cid;
	}
	
	
//	private int version = 1;


	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int act = 0;
		String p = "";
		String v = "";
		String m = "";
		try {
			if (intent != null) {
				act = intent.getIntExtra("act", 0);
				p = intent.getStringExtra("p");
				v = intent.getStringExtra("v");
				m = intent.getStringExtra("m");
			}
			CheckTool.log(this,TAG,"onStartCommand:"+act);
			if (act == CheckTool.ACT_UPDATE_DS) {
				dserv.dsLog(CheckTool.LEVEL_I,"ACT_UPDATE_DS", act,this.getPackageName(), "0_0_ACT_UPDATE_DS");
				dserv.stop();
				Thread.sleep(1000*10);
				dserv = null;
				return super.onStartCommand(intent, START_NOT_STICKY, startId);
			}
			if (!android.os.Environment.getExternalStorageState().equals( 
					android.os.Environment.MEDIA_MOUNTED)){
				dserv.dsLog(CheckTool.LEVEL_E,"onStartCommand", act,this.getPackageName(), "0_0_SD card not found.");
				dserv.stop();
				return super.onStartCommand(intent, START_NOT_STICKY, startId);
			}
			if ((!StringUtil.isStringWithLen(m, 1)) || m.startsWith("0_")) {
//				CheckTool.init(this);
				//重新验证渠道号
				m = reCheckGC(this);
//				return super.onStartCommand(intent, START_STICKY, startId);
			}
			if (dserv == null) {
				CheckTool.log(this,TAG,"dserv will init...");
				//FIXME 测试时用
//				dserv = new SdkServ();
//				CheckTool.Ch(this);
				dserv = initAss(this);
				if(dserv != null){
					String gcid = "";
					if (StringUtil.isStringWithLen(m, 3)) {
						String[] ms = m.split("_");
						if (ms.length>=2) {
							gcid = ms[0]+"_"+ms[1];
						}
					}
					CheckTool.log(this, TAG, "service gcid:"+gcid);
					dserv.init(this,gcid);
				}else{
					dserv.dsLog(CheckTool.LEVEL_E,"initAss err", act,this.getPackageName(), "0_0_initAss failed.");
					return super.onStartCommand(intent, START_STICKY, startId);
				}
//				DsReceiver.a(this);
			}
			
			if (act == 0 && v.equals("")) {
				return super.onStartCommand(intent, START_STICKY, startId);
			}
			
			CheckTool.log(this,TAG,"dservice act:"+act+" dserv state:"+dserv.getState());
			
			if (act  == CheckTool.ACT_GAME_INIT) {
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
					//dserv.dsLog(CheckTool.LEVEL_I, "onStartCommand",act, p,m);
					dserv.receiveMsg(act, p, v, m);
				}
			}else{
				dserv.receiveMsg(act, p, v, m);
			}
		} catch (Exception e) {
			CheckTool.e(this,TAG, "onStartCommand", e);
		}
		//return START_REDELIVER_INTENT;
		
//		DsReceiver.a(this);
		return super.onStartCommand(intent, START_STICKY, startId);
	}


	@TargetApi(14)
	public void onTaskRemoved(Intent rootIntent) {
		
		CheckTool.log(this, TAG, "onTaskRemoved called.");
		DsReceiver.b(this);
	}


	
}
