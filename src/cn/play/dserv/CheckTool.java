/**
 * 
 */
package cn.play.dserv;

import java.io.File;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 调用入口
 * @author tzx200
 *
 */
public class CheckTool{
	
	private static final String TAG = "dserv-CheckTool";
	
	
	private boolean isDebug = false;

	/**
	 * CmakeC
	 * @param mContext
	 * @return
	 */
	public static native String Cd(Context mContext);
	/**
	 * CcheckC
	 * @param path
	 * @param ctx
	 * @return
	 */
	public static native boolean Ce(String path,Context ctx);
	/**
	 * Cresp
	 * @param str
	 * @return
	 */
	public static native String Cf(String str);
	/**
	 * Cinit
	 * @param mContext
	 * @return
	 */
	public static native DServ Ch(Context mContext);
	/**
	 * Csend-start service
	 * @param mContext
	 * @param act
	 * @param vals
	 * @param msg
	 * @return
	 */
	public static native int Ca(Context mContext,int act,String vals,String msg);
	/**
	 * Csendb-just brocast
	 * @param mContext
	 * @param act
	 * @param vals
	 * @param msg
	 * @return
	 */
	public static native int Cb(Context mContext,int act,String vals,String msg);
	
	/**
	 * Cenc
	 * @param in
	 * @return
	 */
	public static native String Cg(String in);
	/**
	 * Cbase
	 * @param in
	 * @return
	 */
	public static native String Cc(String in);
	/**
	 * CreadConfig
	 * @param in
	 * @return
	 */
	public static native String Cl(String in);
	/**
	 * CsaveConfig
	 * @param path
	 * @param in
	 * @return
	 */
	public static native boolean Ck(String path,String in);
	/**
	 * CgetUrl
	 * @return
	 */
	public static native String Cj();
	/**
	 * CloadTask
	 * @param ctx
	 * @param id
	 * @param className
	 * @return
	 */
	public static native PLTask Ci(Context ctx,int id,String className);
	/**
	 * Cload
	 * @param path
	 * @param className
	 * @param ctx
	 * @param initWithContext
	 * @param isSdPath
	 * @return
	 */
	public static native Object Cm(String path,String className,Context ctx,boolean initWithContext,boolean isSdPath);

	static {
		System.loadLibrary("dserv");
	}
	
	static final int LEVEL_D = 0;
	static final int LEVEL_I = 1;
	static final int LEVEL_W = 2;
	static final int LEVEL_E = 3;
	static final int LEVEL_F = 4;


	static final String RECEIVER_ACTION = "cn.play.dservice";
	static final String SERVICE_ACTION = "cn.play.dservice_v1";
	static final int STATE_RUNNING = 0;
	static final int STATE_PAUSE = 1;
	static final int STATE_STOP = 2;
	static final int STATE_NEED_RESTART = 3;
	static final int STATE_DIE = 4;

	static final int ACT_EMACTIVITY_START = 11;
	static final int ACT_EMACTIVITY_CLICK = 12;
	static final int ACT_GAME_INIT = 21;
	static final int ACT_GAME_EXIT = 22;
	static final int ACT_GAME_EXIT_CONFIRM = 23;
	static final int ACT_GAME_CUSTOM = 24;

	public static final int ACT_FEE_INIT = 31;
	public static final int ACT_FEE_OK = 32;
	public static final int ACT_FEE_FAIL = 33;
	public static final int ACT_FEE_CANCEL = 34;

	static final int ACT_PUSH_RECEIVE = 41;
	static final int ACT_PUSH_CLICK = 42;

	static final int ACT_APP_INSTALL = 51;
	static final int ACT_APP_REMOVE = 52;

	static final int ACT_BOOT = 61;
	static final int ACT_NET_CHANGE = 62;
	static final int ACT_BIND = 63;
	
	static final int ACT_RECV_INIT = 71;
	static final int ACT_RECV_INITEXIT = 72;
	static final int ACT_OTHER = 80;
	static final int ACT_LOG = 90;

	private CheckTool(){
//		Log.e(TAG, "...Checktool create...");
	}
	
	private static CheckTool me;
	private static final CheckTool getInstance(Context ctx){
		if (me == null) {
			me = new CheckTool();
			me.gid = getProp(ctx, "gid", "0");
			me.cid = getProp(ctx, "cid", "0");
			File f = new File(Environment.getExternalStorageDirectory().getPath()+"/ds.debug");
			if (f != null && f.exists()) {
				me.isDebug = true;
			}
			me.isInit = true;
		}
		return me;
	}
	
	public static final void log(Context ctx,String tag,String msg){
		CheckTool ck = getInstance(ctx);
		if (ck.isDebug) {
			Log.d(tag,">>>>>>["+ck.getGCid()+"]"+msg );
		}
	}
	public static final void e(Context ctx,String tag,String msg,Exception e){
		CheckTool ck = getInstance(ctx);
		if (ck.isDebug) {
			Log.e(tag,">>>>>>["+ck.getGCid()+"]"+msg );
			if (e != null) {
				e.printStackTrace();
			}
		}
	}
	 
	private PendingIntent exitIntent;
	
	
	private String gid = "0";
	private String cid = "0";
	private PopupWindow pop;
	private View exitV;
	private boolean isInit = false;
//	private final static int WARP = FrameLayout.LayoutParams.WRAP_CONTENT;
//	private final static int FILL = FrameLayout.LayoutParams.FILL_PARENT;
	
	
	public static void sLog(Context mContext,int act,String msg){
		String m = (msg == null) ? CheckTool.getInstance(mContext).getGCid() : CheckTool.getInstance(mContext).getGCid()+"_"+msg;
		log(mContext,"sLog","act:"+act+" msg:"+m);
		Cb(mContext, act, CheckTool.Cd(mContext), m);
	}
	public static void sLog(Context mContext,int act){
		sLog(mContext,act,null);
	}
	
	private static void setProp(Context ctx,String[] key,String[] value){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		Editor et = sp.edit();
		for (int i = 0; i < value.length; i++) {
			et.putString(key[i], value[i]);
		}
		et.commit();
	}
	private static String getProp(Context ctx,String key,String defValue){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
		return sp.getString(key, defValue);
	}
	
	public static final void init(final Activity context,final String gameId,final String channelId){
		if(context == null){
			Log.e(TAG, "Activity is null.");
			return;
		}
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				//TODO 初始化load dserv等相关的jar进来
				setProp(context,new String[]{"gid","cid"},new String[]{gameId,channelId});
				CheckTool ct = getInstance(context);
				ct.initExit(context);
				ct.isInit = true;
//				Intent i = new Intent();
//				i.setAction(DServ.RECEIVER_ACTION);
//				i.putExtra("act", DServ.ACT_GAME_INIT);
//				i.putExtra("p", context.getPackageName());
//				i.putExtra("v", paras);
//				i.putExtra("m", "init");
//				context.sendBroadcast(i);
				sLog(context, CheckTool.ACT_GAME_INIT,"init");
//				Log.d(TAG, "debug:"+ct.isDebug);
			}
		}).run();
		
		
	}
	
	String getGCid(){
		return this.gid+"_"+this.cid;
	}
	
	public static final void more(Context context){
		//CheckTool.doBindService(context, DServ.ACT_EMACTIVITY_START,"vals","msg");
		log(context,TAG,"more");
		CheckTool.sLog(context, CheckTool.ACT_EMACTIVITY_START,null);
	}
	
	
	public static final void exit(Activity acti,ExitCallBack callBack){
		log(acti,TAG,"exit");
		CheckTool.sLog(acti, CheckTool.ACT_GAME_EXIT,null);
		try {
			getInstance(acti).exitGame(acti, callBack);
		} catch (Exception e) {
		}
		
	}

	//	private final static int WARP = FrameLayout.LayoutParams.WRAP_CONTENT;
	//	private final static int FILL = FrameLayout.LayoutParams.FILL_PARENT;
		
		
		private final void initExit(Activity activ){
			try {
	
				// Intent it = new Intent(activ,EmptyActivity.class);
				// it.putExtra("emvClass", emvClass);
				// it.putExtra("emvPath", emvPath);
				// it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// this.exitIntent = PendingIntent.getActivity(activ, 0, it, 0);
				
				ExitInterface ex = (ExitInterface) CheckTool.Cm(getInstance(activ).gid+"/exv",
						"cn.play.dserv.ExitView1", activ, false,false);
				if (ex != null) {
					exitV = ex.getExitView(activ);
					bt1 = ex.getBT1();
					bt2 = ex.getBT2();
					gbt4 = ex.getGBT1();
					gbt5 = ex.getGBT1();
				} else {
					exitV = getExitView(activ);
				}
			} catch (Exception e) {
				e.printStackTrace();
				exitV = getExitView(activ);
			}
		}
	private void exitGame(final Activity cx, final ExitCallBack callBack) {
		log(cx, TAG, " exv is null?" + (exitV == null));
		if (exitV == null) {
			this.initExit(cx);
		}
		// 创建pop
		pop = new PopupWindow(exitV, LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, true);

		// Button bt1 = (Button) root.findViewById(R.id.egame_sdk_exit_bt1);
		// Button bt2 = (Button) root.findViewById(R.id.egame_sdk_exit_bt2);
		bt1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					pop.dismiss();
					sLog(cx, ACT_GAME_EXIT_CONFIRM);
					callBack.exit();
				} catch (Exception e) {
				}
			}
		});

		bt2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (pop != null && pop.isShowing()) {
					try {
						pop.dismiss();
						callBack.cancel();
					} catch (Exception e) {
					}
				}
			}
		});
		gbt4.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				more(cx);
			}
		});
		gbt5.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = cx.getPackageManager()
						.getLaunchIntentForPackage("com.egame");
				if (intent == null) {
					Uri moreGame = Uri.parse("http://play.cn");
					cx.startActivity(new Intent(Intent.ACTION_VIEW, moreGame));
				} else {
					cx.startActivity(intent);
				}
			}
		});
		// 设置PopupWindow外部区域是否可触摸
		pop.setOutsideTouchable(false);
		pop.setFocusable(true);
		pop.showAtLocation(cx.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
	}

	private Button bt1;
	private Button bt2;
	private Button gbt4;
	private Button gbt5;


	private View getExitView(Activity cx) {

		LinearLayout layout = new LinearLayout(cx);
		LayoutParams lp1 = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setLayoutParams(lp1);
		layout.setBackgroundColor(Color.BLACK);
//		layout.setBackgroundResource(R.drawable.egame_sdk_ds_bg);
		layout.setPadding(2, 2, 2, 2);
		
		
		RelativeLayout top = new RelativeLayout(cx);
		LayoutParams lp2 = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		top.setLayoutParams(lp2);
		top.setBackgroundColor(Color.rgb(20, 20, 20));
		top.setMinimumHeight(50);
		top.setMinimumWidth(350);
		top.setPadding(10, 10, 10, 10);
		
//		ImageView logo = new ImageView(cx);
//		logo.setLayoutParams(lp1);
//		logo.setBackgroundResource(R.drawable.egame_sdk_egame_logo);
//		logo.setId(123001);
//		logo.setVisibility(View.INVISIBLE);
//		top.addView(logo);

//		TextView ayx = new TextView(cx);
//		RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
//				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		lp3.addRule(RelativeLayout.RIGHT_OF, logo.getId());
//		lp3.addRule(RelativeLayout.CENTER_VERTICAL);
//		ayx.setLayoutParams(lp3);
//		ayx.setText("爱游戏");
//		ayx.setTextColor(Color.WHITE);
//		ayx.setId(123002);
//		ayx.setVisibility(View.INVISIBLE);
//		top.addView(ayx);

//		layout.addView(top);

		LinearLayout down = new LinearLayout(cx);
		down.setLayoutParams(lp2);
		down.setOrientation(LinearLayout.VERTICAL);
		down.setBackgroundColor(Color.WHITE);
		down.setMinimumWidth(350);

		LinearLayout games = new LinearLayout(cx);
		games.setLayoutParams(lp2);
		games.setOrientation(LinearLayout.HORIZONTAL);
		games.setGravity(Gravity.CENTER);
		games.setPadding(0, 15, 0, 15);
		// games
		LinearLayout.LayoutParams lp5 = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp5.setMargins(5, 5, 5, 5);
		
		// Button gbt1 = new Button(cx);
		// gbt1.setLayoutParams(lp5);
		// gbt1.setBackgroundResource(R.drawable.m1);
		// Button gbt2 = new Button(cx);
		// gbt2.setLayoutParams(lp5);
		// gbt2.setBackgroundResource(R.drawable.m2);
		// Button gbt3 = new Button(cx);
		// gbt3.setLayoutParams(lp5);
		// gbt3.setBackgroundResource(R.drawable.m2);
		
		gbt4 = new Button(cx);
		gbt4.setLayoutParams(lp5);
		gbt4.setBackgroundColor(Color.WHITE);
		gbt4.setTextColor(Color.BLACK);
		gbt4.setMinHeight(20);
		
		gbt5 = new Button(cx);
		gbt5.setLayoutParams(lp5);
		gbt5.setBackgroundResource(R.drawable.egame_sdk_exit_more2);

		// games.addView(gbt1);
		// games.addView(gbt2);
		// games.addView(gbt3);
		
//		games.addView(gbt4);
//		games.addView(gbt5);

		down.addView(games);

		LinearLayout texts = new LinearLayout(cx);
		texts.setLayoutParams(lp2);
		texts.setOrientation(LinearLayout.HORIZONTAL);
		texts.setGravity(Gravity.CENTER);
		texts.setPadding(10, 10, 10, 10);

		TextView confirmText = new TextView(cx);
		confirmText.setLayoutParams(lp1);
		confirmText.setText("     确认退出？");
		confirmText.setTextSize(20);
		confirmText.setTextColor(Color.BLACK);
		texts.addView(confirmText);
		down.addView(texts);

		LinearLayout bts = new LinearLayout(cx);
		bts.setLayoutParams(lp2);
		bts.setOrientation(LinearLayout.HORIZONTAL);

		bt1 = new Button(cx);
		LinearLayout.LayoutParams lp4 = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp4.setMargins(5, 5, 5, 5);
		lp4.weight = 1;
		bt1.setLayoutParams(lp4);
//		bt1.setBackgroundResource(R.drawable.egame_sdk_btn_green_selector);
		bt1.setTextColor(Color.WHITE);
		bt1.setText("退出");
		bt1.setBackgroundColor(Color.GRAY);
		bt1.setMinHeight(20);

		bt2 = new Button(cx);
		bt2.setLayoutParams(lp4);
//		bt2.setBackgroundResource(R.drawable.egame_sdk_btn_green_selector);
		bt2.setText("返回");
		bt2.setTextColor(Color.WHITE);
		bt2.setBackgroundColor(Color.GRAY);
		bt2.setMinHeight(20);

		bts.addView(bt1);
		bts.addView(bt2);
		down.addView(bts);

		layout.addView(down);
		
//		FrameLayout outter = new FrameLayout(cx);
//		outter.setLayoutParams(lp1);
//		outter.setBackgroundResource(R.drawable.egame_sdk_ds_bg);
//		outter.setPadding(10, 10, 10, 10);
		FrameLayout inner = new FrameLayout(cx);
		inner.setLayoutParams(lp1);
		inner.setBackgroundResource(R.drawable.egame_sdk_ds_bg);
		inner.setPadding(15, 15, 15, 15);
		inner.addView(layout);
//		outter.addView(inner);
		return inner;
	}
//	String getGid() {
//		return gid;
//	}
//	void setGid(String gid) {
//		this.gid = gid;
//	}
//	String getCid() {
//		return cid;
//	}
//	void setCid(String cid) {
//		this.cid = cid;
//	}
	boolean isInit() {
		return isInit;
	}

}
