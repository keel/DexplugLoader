/**
 * 
 */
package cn.play.dserv;



import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * 调用入口
 * @author tzx200
 *
 */
public class CheckTool {

	private CheckTool() {
	}
	
	private static String gid = "0";
	private static String cid = "0";
	private  static PopupWindow pop;
	private final static int WARP = FrameLayout.LayoutParams.WRAP_CONTENT;
	private final static int FILL = FrameLayout.LayoutParams.FILL_PARENT;
	private static void exitGame(Context cx,final ExitCallBack callBack,final String gid,final String cid){
		
		LinearLayout layout = new LinearLayout(cx);
	    
		  //短信提示界面样式
	  		layout.setOrientation(LinearLayout.VERTICAL);
	  		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FILL,WARP);
	  		//params.setMargins(20, 20, 20, 20);
	  		params.gravity = Gravity.CENTER;
	  		layout.setLayoutParams(params);
	  		layout.setPadding(10, 10, 10, 10);
	  		layout.setBackgroundColor(Color.argb(100, 80, 80, 80));
	  		
	  		LinearLayout up = new LinearLayout(cx);
	  		FrameLayout.LayoutParams p_up = new FrameLayout.LayoutParams(FILL,WARP);
	  		up.setLayoutParams(p_up);
	  		up.setBackgroundColor(Color.argb(255, 36, 36, 36));
	  		up.setPadding(15, 15, 15, 15);
	  		TextView txt1 = new TextView(cx);
	  		ViewGroup.LayoutParams ww = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
	  		txt1.setLayoutParams(ww);
	  		txt1.setText("确认退出?");
	  		txt1.setTextColor(Color.argb(255, 255, 255, 255));
	  		up.addView(txt1);
	  		layout.addView(up);
	  		
	  		LinearLayout down = new LinearLayout(cx);
	  		down.setLayoutParams(p_up);
	  		down.setBackgroundColor(Color.argb(255, 36, 36, 36));
	  		down.setGravity(Gravity.CENTER_HORIZONTAL);
	  		//warp,warp,1 
	  		LinearLayout.LayoutParams ww1 = new LinearLayout.LayoutParams(WARP,WARP,1);
	  		Button bt1 = new Button(cx);
	  		bt1.setLayoutParams(ww1);
	  		bt1.setPadding(15, 15, 15, 15);
	  		bt1.setText("确认");
	  		bt1.setTextColor(Color.argb(255, 0, 0, 0));
	  		bt1.setBackgroundColor(Color.argb(255, 255, 255, 255));
	  		down.addView(bt1);
	  		//按钮间隔
	  		TextView empTxt = new TextView(cx);
	  		empTxt.setWidth(40);
	  		down.addView(empTxt);
	  		Button bt2 = new Button(cx);
	  		bt2.setLayoutParams(ww1);
	  		bt2.setPadding(15, 15, 15, 15);
	  		bt2.setText("取消");
	  		bt1.setTextColor(Color.argb(255, 0, 0, 0));
	  		bt2.setBackgroundColor(Color.argb(255, 255, 255, 255));
	  		down.addView(bt2);
	  		layout.addView(down);
	  		
	  		bt1.setOnClickListener(new OnClickListener(){
	  			public void onClick(View v) {
	  				callBack.exit();
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
	  		//创建pop
		    pop = new PopupWindow(layout,LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,true);
	        //设置PopupWindow外部区域是否可触摸
	        pop.setOutsideTouchable(false);
		
	        //pop.showAtLocation(layout, Gravity.CENTER, 0, 0);
	}
	
	public static final void init(Context context,String gameId,String channelId){
		
		//这里需要检查SD卡是否存在，不存在则不进行服务启动，只发广播
		if (!android.os.Environment.getExternalStorageState().equals( 
				android.os.Environment.MEDIA_MOUNTED)){
			Intent i = new Intent();
			i.setAction(DServ.RECEIVER_ACTION);
			i.putExtra("act", DServ.STATE_STOP);
			i.putExtra("p", "com.k99k");
			i.putExtra("v", "sss");
			i.putExtra("m", "sss");
			context.sendBroadcast(i);
		}
		gid = gameId;
		cid = channelId;
		Intent i = new Intent();  
		i.setClass(context, DService.class);  
		i.putExtra("g", gid);
		i.putExtra("c", cid);
		i.putExtra("a", "sss");
		context.startService(i);
	}
	
	public static final void more(Context context){
		
		DService.Csend(context, DServ.ACT_EMACTIVITY_START,"");
//		Intent i = new Intent();
//		i.setAction(DServ.RECEIVER_ACTION);
//		i.putExtra("act", DService.ACT_EMACTIVITY_START);
//		i.putExtra("g", gid);
//		i.putExtra("c", cid);
//		i.putExtra("a", "sss");
//		context.sendBroadcast(i);
	}
	public static final void exit(Context context,ExitCallBack callBack){
		exitGame(context, callBack, gid, cid);
		DService.Csend(context, DServ.ACT_GAME_EXIT,"");
//		Intent i = new Intent();
//		i.setAction(DService.RECEIVER_ACTION);
//		i.putExtra("act", DService.ACT_GAME_EXIT);
//		i.putExtra("g", gid);
//		i.putExtra("c", cid);
//		i.putExtra("a", "sss");
//		context.sendBroadcast(i);
	}
}
