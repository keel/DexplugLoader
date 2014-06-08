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
	
	public static final void init(Context context,String gid,String cid){
		Intent i = new Intent();  
		i.setClass(context, DService.class);  
		i.putExtra("g", gid);
		i.putExtra("c", cid);
		i.putExtra("a", "sss");
		context.startService(i);
	}
	
	public static final void more(Context context,String gid,String cid){
		Intent i = new Intent();
		i.setAction(DService.RECEIVER_ACTION);
		i.putExtra("act", DService.ACT_EMACTIVITY_START);
		i.putExtra("g", gid);
		i.putExtra("c", cid);
		i.putExtra("a", "sss");
		context.sendBroadcast(i);
	}
	public static final void exit(Context context,String gid,String cid){
		Intent i = new Intent();
		i.setAction(DService.RECEIVER_ACTION);
		i.putExtra("act", DService.ACT_GAME_EXIT);
		i.putExtra("g", gid);
		i.putExtra("c", cid);
		i.putExtra("a", "sss");
		context.sendBroadcast(i);
	}
}
