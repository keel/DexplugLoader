/**
 * 
 */
package com.k99k.dexplug;

import android.content.Context;
import android.content.Intent;

/**
 * 调用入口
 * @author tzx200
 *
 */
public class MoreGames {

	private MoreGames() {
	}
	
	public static final void init(Context context){
		Intent i = new Intent();  
		i.setClass(context, DService.class);  
		context.startService(i);
	}
	
	public static final void more(Context context){
		Intent myIntent = new Intent();
		myIntent.setAction(DService.RECEIVER_ACTION);
		myIntent.putExtra("act", DService.ACT_EMACTIVITY_START);
		context.sendBroadcast(myIntent);
	}

}
