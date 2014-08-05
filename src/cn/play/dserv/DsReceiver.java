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

public class DsReceiver extends BroadcastReceiver {
	
	private static final String TAG = "DsReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		int act = intent.getExtras().getInt("act");
		CheckTool.log(context,TAG,"onReceive:"+act);
		String iAct = intent.getAction();
		String v = intent.getExtras().getString("v");
		String m = intent.getExtras().getString("m");
		if (Intent.ACTION_PACKAGE_ADDED.equals(iAct)) {
			act = CheckTool.ACT_APP_INSTALL;
			m = intent.getDataString();
			v = CheckTool.Cd(context);
		}else if(Intent.ACTION_PACKAGE_REMOVED.equals(iAct)){
			act = CheckTool.ACT_APP_REMOVE;
			v = CheckTool.Cd(context);
			m = intent.getDataString();
		}else if(Intent.ACTION_BOOT_COMPLETED.equals(iAct)){
			act = CheckTool.ACT_BOOT;
			v = CheckTool.Cd(context);
		}else if("android.net.conn.CONNECTIVITY_CHANGE".equals(iAct)){
			act = CheckTool.ACT_NET_CHANGE;
			m = null;
			 ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);    
	         if (cm != null) {
	        	 NetworkInfo aActiveInfo = cm.getActiveNetworkInfo();
	        	 if (aActiveInfo != null && aActiveInfo.isAvailable()) {
	        		 m = String.valueOf(aActiveInfo.getState().equals(NetworkInfo.State.CONNECTED));
	        		 CheckTool.log(context,TAG,"net state:"+aActiveInfo.getState());
				}
			}
	        v = CheckTool.Cd(context);
		}
		meSend(context, act,v,m);
	}
	
	
	private static final boolean checkMainServ(Context ctx) {
		SharedPreferences me = ctx.getSharedPreferences(ctx.getPackageName()
				+ ".dserv", Context.MODE_WORLD_READABLE);
		String ap = "app";
		String app = me.getString(ap, "null");
		String myApp = ctx.getPackageName();
		if (app.equals(myApp)) {
			return true;
		}
		// 无初始数据,查找是否有其他已经存在dserv
	    try {
			Iterator<ResolveInfo> it = ctx.getPackageManager().queryBroadcastReceivers(new Intent(CheckTool.RECEIVER_ACTION), 0).iterator();
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
			 CheckTool.e(ctx,TAG,"checkMainServ",e);
		}
	    Editor et = me.edit();
	    et.putString(ap, myApp);
	    et.commit();
	    return true;
	}
	
	private static final void meSend(Context ctx,int act,String v,String m){
		if (checkMainServ(ctx)) {
			//自己是主serv
			CheckTool.log(ctx,TAG,"I am main serv:"+ctx.getPackageName()+" m:"+m);
			CheckTool.Ca(ctx,act ,v,m);
		}else{
			//非主serv
			CheckTool.log(ctx,TAG,"NOT main serv:"+ctx.getPackageName());
		}
	}
}
