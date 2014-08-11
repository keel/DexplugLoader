/**
 * 
 */
package cn.play.dserv;

import android.app.Service;
import android.os.Handler;


/**
 * @author tzx200
 *
 */
public interface DServ {

	
	public void init(DService dserv);
	
	
	public void saveStates();
	
	
	public void stop();
	
	public int getState();
	
	public Service getService();
	
	public Handler getHander();
	
	public void log(int level,String tag,int act,String pkg,String msg);
	
	public void receiveMsg(int act,String p,String v,String m);
	
	public int getVer();
}
