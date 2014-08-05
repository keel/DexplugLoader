package cn.play.dserv;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class MoreView implements EmView {
	
	private Activity context;

	public MoreView(Context context) {
		init(context);
	}

	@Override
	public View getView() {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		
		ScrollView layout = new ScrollView(this.context);
		layout.setPadding(15, 15, 15,15);
		layout.setBackgroundResource(R.drawable.egame_sdk_ds_bg);
//		ScrollView scroll = new ScrollView(context);
//		scroll.setLayoutParams(lp);
//		scroll.setPadding(15, 15, 15,15);
//		scroll.setBackgroundColor(Color.rgb(230, 230, 230));
		LinearLayout out = new LinearLayout(this.context);
		out.setOrientation(LinearLayout.VERTICAL);
		out.setLayoutParams(lp);
		out.setPadding(15, 15, 15,15);
		out.setBackgroundColor(Color.rgb(230, 230, 230));
		LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,10);
		
		for (int i = 0; i < 4; i++) {
			out.addView(this.one(this.context, i, "test"+i, "sub", "最终季暗黑归来，白老师腹黑到底，别惹老师。 《绝命毒师》第五季剧情梗概： 化学老师Walter White (Bryan Cranston饰演)中学化学老师，少言寡语、性格温驯、安分守己、循", "http://baidu.com?a="+i));
			LinearLayout split = new LinearLayout(this.context);
			split.setBackgroundColor(Color.rgb(230, 230, 230));
			split.setLayoutParams(sp);
			out.addView(split);
		}
		LinearLayout close = new LinearLayout(this.context);
		close.setPadding(20, 30, 30, 20);
		close.setGravity(Gravity.CENTER);
		close.setBackgroundColor(Color.WHITE);
		TextView t_close = new TextView(this.context);
		t_close.setText("知道了");
		t_close.setTextColor(Color.BLACK);
		close.addView(t_close);
		close.setOnClickListener(btClose);
		out.addView(close);
		layout.addView(out);
		return layout;
	}
	
	private View one(Context ctx,int pic,String name,String sub,String txt,final String downUrl){
		RelativeLayout out = new RelativeLayout(ctx);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		
		
		android.widget.RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		ImageView iconView = new ImageView(context);
		iconView.setImageBitmap(loadImg(pic));
		iconView.setId(1001);
		iconView.setPadding(10, 10, 10, 10);
		iconView.setContentDescription("icon");
		out.addView(iconView,lp2);
		
		android.widget.RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp3.setMargins(0, 10, 0, 0);
		lp3.addRule(RelativeLayout.RIGHT_OF,iconView.getId());
		lp3.addRule(RelativeLayout.ALIGN_TOP,iconView.getId());
		TextView appName = new TextView(context);
		appName.setTextSize(20);
		appName.setText(name);
		appName.setTextColor(Color.BLACK);
		appName.setId(1002);
		out.addView(appName,lp3);
		
		android.widget.RelativeLayout.LayoutParams lp4 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp4.addRule(RelativeLayout.RIGHT_OF,iconView.getId());
		lp4.addRule(RelativeLayout.BELOW,appName.getId());
		TextView subInfo = new TextView(context);
		subInfo.setTextSize(12);
		subInfo.setText(sub);
		subInfo.setTextColor(Color.GRAY);
		subInfo.setId(1003);
		out.addView(subInfo,lp4);
		
		android.widget.RelativeLayout.LayoutParams lp5 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp5.setMargins(0, 0, 15, 0);
		lp5.addRule(RelativeLayout.ALIGN_LEFT,subInfo.getId());
		lp5.addRule(RelativeLayout.BELOW,subInfo.getId());
		TextView info = new TextView(context);
		info.setTextSize(12);
		info.setText(txt);
		info.setTextColor(Color.GRAY);
		info.setId(1004);
		out.addView(info,lp5);
		
		
		android.widget.RelativeLayout.LayoutParams lp6 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp6.setMargins(0, 0, 10, 0);
		lp6.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
		lp6.addRule(RelativeLayout.ALIGN_TOP,appName.getId());
		Button down = new Button(context);
		down.setTextSize(15);
		down.setText("下载");
		down.setTextColor(Color.WHITE);
		down.setBackgroundColor(Color.rgb(10, 200, 10));
		down.setId(1005);
		down.setPadding(25, 10, 25, 10);
		
		down.setOnClickListener(new BtDown(downUrl));
		out.addView(down,lp6);
		out.setBackgroundColor(Color.WHITE);
		out.setLayoutParams(lp);
		return out;
	}
	
	public void close(){
		this.context.finish();
	}
	
	BtClose btClose = new BtClose();
	
	public class BtClose implements OnClickListener{
		@Override
		public void onClick(View v) {
			close();
		}
		
	}

	public class BtDown implements OnClickListener{

		private String url;
		public BtDown(String u){
			this.url = u;
		}
		@Override
		public void onClick(View v) {
			CheckTool.log(context,"More", url);			
		}
		
	}
	
	

	private Bitmap loadImg(int i){
		String imgPath = Environment.getExternalStorageDirectory().getPath()+"/.dserver/m"+(i+1)+".png";
		return BitmapFactory.decodeFile(imgPath);
	}

	@Override
	public void init(Context ctx) {
		this.context = (Activity) ctx;
	}

	
}
