/**
 * 
 */
package cn.play.dserv;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
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
	
	private static final String TAG = "CheckTool";
	

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
	public static native Object Cload(String path,String className,Context ctx,boolean initWithContext,boolean isSdPath);

	static {
		System.loadLibrary("dserv");
	}
	
	private CheckTool(){
		Log.e(TAG, "...Checktool create...");
	}
	
	private static CheckTool me;
	private static final CheckTool getInstance(){
		if (me == null) {
			me = new CheckTool();
		}
		return me;
	}
	
	 
	private PendingIntent exitIntent;
	
	
	private String gid = "0";
	private String cid = "0";
	private PopupWindow pop;
	private View exitV;
	private boolean isInit = false;
//	private final static int WARP = FrameLayout.LayoutParams.WRAP_CONTENT;
//	private final static int FILL = FrameLayout.LayoutParams.FILL_PARENT;
	
	
	public static final void init(final Activity context,final String gameId,final String channelId){
		if(context == null){
			Log.e(TAG, "Activity is null.");
			return;
		}
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				//FIXME 初始化load dserv等相关的jar进来
				
				
				CheckTool ct = getInstance();
				ct.initExit(context);
				ct.setGid(gameId);//.gid = gameId;
				ct.setCid(channelId);//.cid = channelId;
				String paras = gameId+"_"+channelId;
				
				Intent i = new Intent();
				i.setAction(DServ.RECEIVER_ACTION);
				i.putExtra("act", DServ.ACT_GAME_INIT);
				i.putExtra("p", context.getPackageName());
				i.putExtra("v", paras);
				i.putExtra("m", "init");
				context.sendBroadcast(i);
			}
		}).run();
		
		
	}
	
	public static final void more(Context context){
		//CheckTool.doBindService(context, DServ.ACT_EMACTIVITY_START,"vals","msg");
		Log.d(TAG, "more:"+getInstance().getGid());
		
		CheckTool.Csendb(context, DServ.ACT_EMACTIVITY_START,"vals","msg");
	}
	
	
	public static final void exit(Activity acti,ExitCallBack callBack){
		Log.d(TAG, "exit:"+getInstance().getGid());

		CheckTool.Csendb(acti, DServ.ACT_GAME_EXIT,"vals","msg");
		
		getInstance().exitGame(acti, callBack);
		
		
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
	
				
				//FIXME 每个游戏退出的包不同，需要初始化时将jar复制到data目录再load
				
				ExitInterface ex = (ExitInterface) CheckTool.Cload("exv",
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
		Log.d(TAG, " exitV is null?" + (exitV == null));

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

		RelativeLayout top = new RelativeLayout(cx);
		LayoutParams lp2 = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		top.setLayoutParams(lp2);
		top.setBackgroundResource(R.drawable.egame_sdk_popup_title);

		ImageView logo = new ImageView(cx);
		logo.setLayoutParams(lp1);
		logo.setBackgroundResource(R.drawable.egame_sdk_egame_logo);
		logo.setId(123001);
//		top.addView(logo);

		TextView ayx = new TextView(cx);
		RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp3.addRule(RelativeLayout.RIGHT_OF, logo.getId());
		lp3.addRule(RelativeLayout.CENTER_VERTICAL);
		ayx.setLayoutParams(lp3);
		ayx.setText("爱游戏");
		ayx.setTextColor(Color.WHITE);
		ayx.setId(123002);
//		top.addView(ayx);

		layout.addView(top);

		LinearLayout down = new LinearLayout(cx);
		down.setLayoutParams(lp2);
		down.setOrientation(LinearLayout.VERTICAL);
		down.setBackgroundResource(R.drawable.egame_sdk_popup_white_bg);

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
		gbt4.setBackgroundResource(R.drawable.egame_sdk_exit_more1);
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
		bt1.setBackgroundResource(R.drawable.egame_sdk_btn_green_selector);
		bt1.setText("退出");
		bt1.setTextColor(Color.WHITE);

		bt2 = new Button(cx);
		bt2.setLayoutParams(lp4);
		bt2.setBackgroundResource(R.drawable.egame_sdk_btn_green_selector);
		bt2.setText("返回");
		bt2.setTextColor(Color.WHITE);

		bts.addView(bt1);
		bts.addView(bt2);
		down.addView(bts);

		layout.addView(down);
		return layout;
	}
	public String getGid() {
		return gid;
	}
	public void setGid(String gid) {
		this.gid = gid;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public boolean isInit() {
		return isInit;
	}

}
