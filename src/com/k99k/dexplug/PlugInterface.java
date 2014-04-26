/**
 * 
 */
package com.k99k.dexplug;

import android.content.Context;
import android.view.View;

/**
 * @author tzx200
 *
 */
public interface PlugInterface {
	
	

	public View plugView(Context context,String p2,String p3);
	
	public boolean init(Context context,String p2,String p3);
	
	public boolean isServiceNeed();
	
	/**
	 * 执行的类型,如下载更新,增删任务,关闭service等
	 * @return
	 */
	public int getType();
	
	public PLTask getTask();
	
	public String getDownloadUrl();
	
	public String getDownloadPath();
	
	
}
