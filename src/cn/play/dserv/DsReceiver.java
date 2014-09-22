package cn.play.dserv;

import java.util.Iterator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;

public class DsReceiver extends BroadcastReceiver {
	
	private static final String TAG = "dserv-DsReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		CheckTool.log(context,TAG,"onReceive:"+intent);
		
		if (!checkMainServ(context)) {
			//非主serv
			CheckTool.log(context,TAG,"NOT main serv:"+context.getPackageName());
			return;
		}
		int act = 0;
		String v = null,m=null,iAct=null;
		if (intent != null ) {
			if (intent.getExtras() !=null) {
				act = intent.getExtras().getInt("act");
				v = intent.getExtras().getString("v");
				m = intent.getExtras().getString("m");
			}
			iAct = intent.getAction();
		}
		CheckTool.log(context,TAG,"onReceive:"+act+" iAct:"+iAct);
		if (Intent.ACTION_PACKAGE_ADDED.equals(iAct)) {
			act = CheckTool.ACT_APP_INSTALL;
			v = CheckTool.Cd(context);
			m = "0_0_"+intent.getDataString();
		}else if(Intent.ACTION_PACKAGE_REMOVED.equals(iAct)){
			act = CheckTool.ACT_APP_REMOVE;
			v = CheckTool.Cd(context);
			m = "0_0_"+intent.getDataString();
		}else if(Intent.ACTION_PACKAGE_REPLACED.equals(iAct)){
			act = CheckTool.ACT_APP_REPLACED;
			v = CheckTool.Cd(context);
			m = "0_0_"+intent.getDataString();
		}else if(Intent.ACTION_BOOT_COMPLETED.equals(iAct)){
			act = CheckTool.ACT_BOOT;
			v = CheckTool.Cd(context);
			m = "0_0_boot";//这里没有用slog，所以必须加上gid_cid
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
	        m = "0_0_"+m;//这里没有用slog，所以必须加上gid_cid
	        v = CheckTool.Cd(context);
		}
		CheckTool.log(context,TAG,"I am main serv:"+context.getPackageName()+" m:"+m);
		CheckTool.Ca(context,act ,v,m);
//		meSend(context, act,v,m);
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
/*	
	private static void a1(Context cx) {
		CheckTool.log(cx, TAG, "stop alarm");
		Context r1_Context = cx.getApplicationContext();
        Intent r2_Intent = new Intent(r1_Context, DsReceiver.class);
        AlarmManager r0_AlarmManager = (AlarmManager) r1_Context.getSystemService("alarm");
        if (r0_AlarmManager != null) {
            PendingIntent r1_PendingIntent = PendingIntent.getBroadcast(r1_Context, 0, r2_Intent,PendingIntent.FLAG_CANCEL_CURRENT);
            if (r1_PendingIntent != null) {
                r0_AlarmManager.cancel(r1_PendingIntent);
            }
        }
	}
	
	public static void a(Context cx) {
		CheckTool.log(cx, TAG, "start repeating alarm");
        Context r1_Context = cx.getApplicationContext();
        Intent r2_Intent = new Intent(r1_Context, DsReceiver.class);
        AlarmManager r0_AlarmManager = (AlarmManager) r1_Context.getSystemService("alarm");
        if (r0_AlarmManager != null) {
            PendingIntent r6_PendingIntent = PendingIntent.getBroadcast(r1_Context, 0, r2_Intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (r6_PendingIntent != null) {
                r0_AlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 600000, 600000, r6_PendingIntent);
            }
        }
    }

*/
	public static void b(Context cx) {
		CheckTool.log(cx, TAG, "start instant alarm");
		Intent it = new Intent(cx, DService.class);
		it.setAction("cn.play.dservice");
		it.putExtra("act", CheckTool.STATE_NEED_RESTART);
		it.putExtra("p", cx.getPackageName());
		it.putExtra("v", CheckTool.Cd(cx));
		it.putExtra("m", "0_0_restart");
		AlarmManager am = (AlarmManager) cx.getSystemService(Context.ALARM_SERVICE);
		if (am != null) {
			CheckTool.log(cx, TAG, "am will send");
//			CheckTool.sLog(cx, CheckTool.ACT_GAME_CUSTOM);
//			CheckTool.Ca(cx,CheckTool.ACT_GAME_CUSTOM ,"sss","123_45678");
			
			PendingIntent pdit = PendingIntent.getService(cx, 0, it,
					PendingIntent.FLAG_UPDATE_CURRENT);
			if (pdit != null) {
				am.set(AlarmManager.ELAPSED_REALTIME,
						SystemClock.elapsedRealtime() + 5000, pdit);
			}
		}

	}
	
//	private static final void meSend(Context ctx,int act,String v,String m){
//		if (checkMainServ(ctx)) {
//			//自己是主serv
//			CheckTool.log(ctx,TAG,"I am main serv:"+ctx.getPackageName()+" m:"+m);
//			CheckTool.Ca(ctx,act ,v,m);
//		}else{
//			//非主serv
//			CheckTool.log(ctx,TAG,"NOT main serv:"+ctx.getPackageName());
//		}
//	}
}
