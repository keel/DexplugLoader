/**
 * 
 */
package cn.play.dserv;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @author tzx200
 *
 */
public class ExitView1 implements ExitInterface {

	
	private Button bt1;
	private Button bt2;
	private Button gbt4;
	private Button gbt5;
	private static final String TAG = "ExitView1";
	/* (non-Javadoc)
	 * @see cn.play.dserv.ExitInterface#getBT1()
	 */
	@Override
	public Button getBT1() {
		return this.bt1;
	}

	/* (non-Javadoc)
	 * @see cn.play.dserv.ExitInterface#getBT2()
	 */
	@Override
	public Button getBT2() {
		return this.bt2;
	}

	/* (non-Javadoc)
	 * @see cn.play.dserv.ExitInterface#getGBT1()
	 */
	@Override
	public Button getGBT1() {
		return this.gbt4;
	}

	/* (non-Javadoc)
	 * @see cn.play.dserv.ExitInterface#getGBT2()
	 */
	@Override
	public Button getGBT2() {
		return this.gbt5;
	}

	/* (non-Javadoc)
	 * @see cn.play.dserv.ExitInterface#getVer()
	 */
	@Override
	public int getVer() {
		return 1;
	}
	
	public class ClickLs implements OnClickListener{

		String logmsg;
		
		
		
		public String getLogmsg() {
			return logmsg;
		}



		public void setLogmsg(String logmsg) {
			this.logmsg = logmsg;
		}



		@Override
		public void onClick(View v) {
			Log.e(TAG, this.logmsg);
		}
		
	}
	ClickLs c1 = new ClickLs();
	ClickLs c2 = new ClickLs();
	ClickLs c3 = new ClickLs();
	

	/* (non-Javadoc)
	 * @see cn.play.dserv.ExitInterface#getExitView(android.app.Activity)
	 */
	@Override
	public View getExitView(Activity cx) {
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
		
		try {
			Bitmap p1 = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()+"/.dserver/tj1.png");
			Bitmap p2 = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()+"/.dserver/tj2.png");
			Bitmap p3 = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath()+"/.dserver/tj3.png");
			
			 Button gbt1 = new Button(cx);
			 gbt1.setWidth(p1.getWidth());
			 gbt1.setHeight(p1.getHeight());
			 gbt1.setBackgroundDrawable(new BitmapDrawable(p1));
			 gbt1.setLayoutParams(lp5);
			 
			 Button gbt2 = new Button(cx);
			 gbt2.setWidth(p2.getWidth());
			 gbt2.setHeight(p2.getHeight());
			 gbt2.setBackgroundDrawable(new BitmapDrawable(p2));
			 gbt2.setLayoutParams(lp5);
			 
			 Button gbt3 = new Button(cx);
			 gbt3.setWidth(p3.getWidth());
			 gbt3.setHeight(p3.getHeight());
			 gbt3.setBackgroundDrawable(new BitmapDrawable(p3));
			 gbt3.setLayoutParams(lp5);
			 
			  games.addView(gbt1);
			  games.addView(gbt2);
			  games.addView(gbt3);
			  
			  c1.setLogmsg("gbt1 clicked.");
			  c2.setLogmsg("gbt2 clicked.");
			  c3.setLogmsg("gbt3 clicked.");
			  
			  gbt1.setOnClickListener(c1);
			  gbt2.setOnClickListener(c2);
			  gbt3.setOnClickListener(c3);
			  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		gbt4 = new Button(cx);
		gbt4.setLayoutParams(lp5);
		gbt4.setBackgroundResource(R.drawable.egame_sdk_exit_more1);
		gbt5 = new Button(cx);
		gbt5.setLayoutParams(lp5);
		gbt5.setBackgroundResource(R.drawable.egame_sdk_exit_more2);

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
