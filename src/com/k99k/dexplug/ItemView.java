package com.k99k.dexplug;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ItemView extends RelativeLayout {

	
	public ItemView(Context context,ItemData item) {
		super(context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		lp.setMargins(1, 1, 1, 1);
		this.setLayoutParams(lp);
		this.setBackgroundColor(Color.rgb(255, 255, 255));
		
		LayoutParams lp2 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		ImageView iconView = new ImageView(context);
		iconView.setImageBitmap(item.getIcon());
		iconView.setId(1001);
		iconView.setPadding(10, 10, 10, 10);
		iconView.setContentDescription("icon");
		addView(iconView,lp2);
		
		LayoutParams lp3 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp3.setMargins(0, 10, 0, 0);
		lp3.addRule(RelativeLayout.RIGHT_OF,iconView.getId());
		lp3.addRule(RelativeLayout.ALIGN_TOP,iconView.getId());
		TextView appName = new TextView(context);
		appName.setTextSize(20);
		appName.setText(item.getAppName());
		appName.setId(1002);
		addView(appName,lp3);
		
		LayoutParams lp4 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp4.addRule(RelativeLayout.RIGHT_OF,iconView.getId());
		lp4.addRule(RelativeLayout.BELOW,appName.getId());
		TextView subInfo = new TextView(context);
		subInfo.setTextSize(12);
		subInfo.setText(item.getSubInfo());
		subInfo.setId(1003);
		addView(subInfo,lp4);
		
		LayoutParams lp5 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp5.setMargins(0, 0, 15, 0);
		lp5.addRule(RelativeLayout.ALIGN_LEFT,subInfo.getId());
		lp5.addRule(RelativeLayout.BELOW,subInfo.getId());
		TextView info = new TextView(context);
		info.setTextSize(12);
		info.setText(item.getInfo());
		info.setId(1004);
		addView(info,lp5);
		
		
		LayoutParams lp6 = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp6.setMargins(0, 0, 10, 0);
		lp6.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
		lp6.addRule(RelativeLayout.ALIGN_TOP,appName.getId());
		TextView down = new TextView(context);
		down.setTextSize(15);
		down.setText("下载");
		down.setId(1005);
		down.setPadding(25, 10, 25, 10);
		down.setBackgroundColor(Color.rgb(10, 200, 10));
		addView(down,lp6);
		
	}


}
