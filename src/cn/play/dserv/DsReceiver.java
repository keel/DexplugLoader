package cn.play.dserv;

import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class DsReceiver extends BroadcastReceiver {
	
	private static final String TAG = "DsReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		int act = intent.getExtras().getInt("act");
		Log.w(TAG, "receive act:"+act);
		switch (act) {
		case DServ.ACT_RECV_INIT:
//			init(acti, gid, cid);
			return;

		case DServ.ACT_RECV_INITEXIT:
//			DService.needExitInit = true;
//			DService.getSharedConf().put("needExitInit", true);
//			Log.d(TAG, "needInitExit SET:"+DService.getSharedConf().get("needExitInit"));
//			initExit(acti);
			return;
		}
		String iAct = intent.getAction();
		String m = intent.getExtras().getString("m");
		if (Intent.ACTION_PACKAGE_ADDED.equals(iAct)) {
			act = DServ.ACT_APP_INSTALL;
		}else if(Intent.ACTION_PACKAGE_REMOVED.equals(iAct)){
			act = DServ.ACT_APP_REMOVE;
		}else if(Intent.ACTION_BOOT_COMPLETED.equals(iAct)){
			act = DServ.ACT_BOOT;
		}else if("android.net.conn.CONNECTIVITY_CHANGE".equals(iAct)){
			act = DServ.ACT_NET_CHANGE;
			m = null;
			 ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);    
	         if (cm != null) {
	        	 NetworkInfo aActiveInfo = cm.getActiveNetworkInfo();
	        	 if (aActiveInfo != null && aActiveInfo.isAvailable()) {
	        		 m = String.valueOf(aActiveInfo.getState().equals(NetworkInfo.State.CONNECTED));
	        		 Log.d(TAG, "net state:"+aActiveInfo.getState());
				}
			}
		}
		meSend(context, act,intent.getExtras().getString("v"),m);
	}
	private static final boolean checkMainServ(Context ctx) {
		SharedPreferences me = ctx.getSharedPreferences(ctx.getPackageName()
				+ ".dserv", Context.MODE_PRIVATE);
		String ap = "app";
		String app = me.getString(ap, "null");
		String myApp = ctx.getPackageName();
		if (app.equals(myApp)) {
			return true;
		}
		// 无初始数据,查找是否有其他已经存在dserv
	    try {
			Iterator<ResolveInfo> it = ctx.getPackageManager().queryBroadcastReceivers(new Intent(DServ.RECEIVER_ACTION), 0).iterator();
			while (it.hasNext()) {
				ResolveInfo ri = it.next();
				String pn = ri.activityInfo.packageName;
				String otherApp = ctx
						.createPackageContext(pn,
								Context.CONTEXT_IGNORE_SECURITY)
						.getSharedPreferences(pn + ".dserv",
								Context.MODE_WORLD_READABLE)
						.getString(ap, "null");
				if (!"null".equals(otherApp)) {
//					Editor et = me.edit();
//					et.putString(ap, otherApp);
//					et.commit();
					return false;
				}
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	    Editor et = me.edit();
	    et.putString(ap, myApp);
	    et.commit();
	    return true;
	}
	
	private static final void meSend(Context ctx,int act,String v,String m){
		if (checkMainServ(ctx)) {
			//自己是主serv
			Log.e(TAG, "I am main serv:"+ctx.getPackageName());
			CheckTool.Csend(ctx,act ,v,m);
		}else{
			//非主serv
			Log.e(TAG, "NOT main serv. finished:"+ctx.getPackageName());
		}
	}
}
