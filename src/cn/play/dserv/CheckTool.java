/**
 * 
 */
package cn.play.dserv;



import android.app.Activity;
import android.content.BroadcastReceiver;
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
public class CheckTool extends BroadcastReceiver{
	
	private static final String TAG = "CheckTool";
	
//	private static String gid = "0";
//	private static String cid = "0";
	private  static PopupWindow pop;

	private static View exitV;
//	private final static int WARP = FrameLayout.LayoutParams.WRAP_CONTENT;
//	private final static int FILL = FrameLayout.LayoutParams.FILL_PARENT;
	public static final void init(Activity context,String gameId,String channelId){
		
		//这里需要检查SD卡是否存在，不存在则不进行服务启动，只发广播
		if (!android.os.Environment.getExternalStorageState().equals( 
				android.os.Environment.MEDIA_MOUNTED)){
			/*
			Intent i = new Intent();
			i.setAction(DServ.RECEIVER_ACTION);
			i.putExtra("act", DServ.STATE_STOP);
			i.putExtra("p", "com.k99k");
			i.putExtra("v", "sss");
			i.putExtra("m", "sss");
			context.sendBroadcast(i);
			*/
			Log.w(TAG, "no sdcard");
			return;
		}
		String paras = gameId+"_"+channelId;
		
		ExitInterface ex;
		try {
			DService.Csend(context, DServ.ACT_GAME_INIT,paras,"init");
			ex = (ExitInterface) DService.Cload("exv", "cn.play.dserv.ExitView1", context);
			if (ex != null) {
				exitV = ex.getExitView(context);
				bt1 = ex.getBT1();
				bt2 = ex.getBT2();
				gbt4 = ex.getGBT1();
				gbt5 = ex.getGBT1();
			}else{
				exitV = getExitView(context);
			}
		} catch (Exception e) {
			e.printStackTrace();
			exitV = getExitView(context);
		}
		/*
		Intent i = new Intent();  
		i.setClass(context, DService.class);  
		i.putExtra("g", gid);
		i.putExtra("c", cid);
		i.putExtra("a", "sss");
		context.startService(i);
		*/
	}
	
	public static final void more(Context context){
		
		DService.Csend(context, DServ.ACT_EMACTIVITY_START,"vals","msg");
//		Intent i = new Intent();
//		i.setAction(DServ.RECEIVER_ACTION);
//		i.putExtra("act", DServ.ACT_EMACTIVITY_START);
//		i.putExtra("g", gid);
//		i.putExtra("c", cid);
//		i.putExtra("a", "sss");
//		context.sendBroadcast(i);
	}
	public static final void exit(Activity acti,ExitCallBack callBack){
		
		DService.Csend(acti, DServ.ACT_GAME_EXIT,"vals","msg");
		
		exitGame(acti, callBack);
		
		
		
//		Intent i = new Intent();
//		i.setAction(DService.RECEIVER_ACTION);
//		i.putExtra("act", DService.ACT_GAME_EXIT);
//		i.putExtra("g", gid);
//		i.putExtra("c", cid);
//		i.putExtra("a", "sss");
//		context.sendBroadcast(i);
		
		
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		int act = intent.getExtras().getInt("act");
		Log.w(TAG, "receive act:"+act);
		
		DService.Csend(context, act,intent.getExtras().getString("v"),intent.getExtras().getString("m"));
		/*
		Intent i = new Intent();
		i.setClass(context, DService.class);
		i.putExtra("act", act);
		i.putExtra("p", intent.getExtras().getString("p"));
		i.putExtra("v", intent.getExtras().getString("v"));
		i.putExtra("m", intent.getExtras().getString("m"));
		context.startService(i);*/
	}
	
	private static void exitGame(final Activity cx,final ExitCallBack callBack){
			
		
		
			/*
			LinearLayout layout = new LinearLayout(cx);
			LayoutParams lp1 = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.setLayoutParams(lp1);
			
			RelativeLayout top  = new RelativeLayout(cx);
			LayoutParams lp2 = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
			top.setLayoutParams(lp2);
			top.setBackgroundResource(R.drawable.egame_sdk_popup_title);
			
			ImageView logo = new ImageView(cx);
			logo.setLayoutParams(lp1);
			logo.setBackgroundResource(R.drawable.egame_sdk_egame_logo);
			logo.setId(123001);
			top.addView(logo);
			
			TextView ayx  = new TextView(cx);
			RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			lp3.addRule(RelativeLayout.RIGHT_OF,logo.getId());
			lp3.addRule(RelativeLayout.CENTER_VERTICAL);
			ayx.setLayoutParams(lp3);
			ayx.setText("爱游戏");
			ayx.setTextColor(Color.WHITE);
			ayx.setId(123002);
			top.addView(ayx);
			
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
			//games
			LinearLayout.LayoutParams lp5 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			lp5.setMargins(5, 5, 5, 5);
			Button gbt1 = new Button(cx);
			gbt1.setLayoutParams(lp5);
			gbt1.setBackgroundResource(R.drawable.m1);
			Button gbt2 = new Button(cx);
			gbt2.setLayoutParams(lp5);
			gbt2.setBackgroundResource(R.drawable.m2);
			Button gbt3 = new Button(cx);
			gbt3.setLayoutParams(lp5);
			gbt3.setBackgroundResource(R.drawable.m2);
			Button gbt4 = new Button(cx);
			gbt4.setLayoutParams(lp5);
			gbt4.setBackgroundResource(R.drawable.egame_sdk_exit_more1);
			Button gbt5 = new Button(cx);
			gbt5.setLayoutParams(lp5);
			gbt5.setBackgroundResource(R.drawable.egame_sdk_exit_more2);
			
	//		games.addView(gbt1);
	//		games.addView(gbt2);
	//		games.addView(gbt3);
			games.addView(gbt4);
			games.addView(gbt5);
			
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
			
			Button bt1 = new Button(cx);
			LinearLayout.LayoutParams lp4 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			lp4.setMargins(5, 5, 5, 5);
			lp4.weight = 1;
			bt1.setLayoutParams(lp4);
			bt1.setBackgroundResource(R.drawable.egame_sdk_btn_green_selector);
			bt1.setText("退出");
			bt1.setTextColor(Color.WHITE);
			
			Button bt2 = new Button(cx);
			bt2.setLayoutParams(lp4);
			bt2.setBackgroundResource(R.drawable.egame_sdk_btn_green_selector);
			bt2.setText("返回");
			bt2.setTextColor(Color.WHITE);
			
			bts.addView(bt1);
			bts.addView(bt2);
			down.addView(bts);
			
			layout.addView(down);
			*/
			
	//		LayoutInflater layoutInflater = (LayoutInflater) cx.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);  
	//		View root = layoutInflater.inflate(R.layout.egame_sdk_exit_layout, null); 
			
	//		ExitInterface exit = DService.getExit(cx);
	//		if (exit == null ) {
	//			Log.e(TAG, "EXIT is null");
	//			return;
	//		}
	//		View v = exit.getExitView(cx);
	//		if (v == null) {
	//			Log.e(TAG, "getExitView is null");
	//			return;
	//		}
			if (exitV == null) {
				exitV = getExitView(cx);
			}
		  		//创建pop
			pop = new PopupWindow(exitV,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,true);
			    
	//		Button bt1 = (Button) root.findViewById(R.id.egame_sdk_exit_bt1);
	//		Button bt2 = (Button) root.findViewById(R.id.egame_sdk_exit_bt2);
			    bt1.setOnClickListener(new OnClickListener(){
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
		  				Intent intent = cx.getPackageManager().getLaunchIntentForPackage(
								"com.egame");
					if (intent == null) {
						Uri moreGame = Uri.parse("http://play.cn");
						cx.startActivity(new Intent(Intent.ACTION_VIEW, moreGame));
					}else{
						cx.startActivity(intent);
					}
		  			}
		  		});
		        //设置PopupWindow外部区域是否可触摸
		        pop.setOutsideTouchable(false);
		        pop.setFocusable(true);
		        pop.showAtLocation(cx.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
		}

	static Button bt1;
	static Button bt2;
	static Button gbt4;
	static Button gbt5;

	public static View getExitView(Activity cx) {

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
		top.addView(logo);

		TextView ayx = new TextView(cx);
		RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp3.addRule(RelativeLayout.RIGHT_OF, logo.getId());
		lp3.addRule(RelativeLayout.CENTER_VERTICAL);
		ayx.setLayoutParams(lp3);
		ayx.setText("爱游戏");
		ayx.setTextColor(Color.WHITE);
		ayx.setId(123002);
		top.addView(ayx);

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
		games.addView(gbt4);
		games.addView(gbt5);

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

}
