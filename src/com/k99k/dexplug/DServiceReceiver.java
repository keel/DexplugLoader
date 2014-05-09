/**
 * 
 */
package com.k99k.dexplug;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author tzx200
 *
 */
public class DServiceReceiver extends BroadcastReceiver {

	/**
	 * 
	 */
	public DServiceReceiver() {
	}

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i = new Intent();  
		i.setClass(context, DService.class);  
		context.startService(i);
	}

}
