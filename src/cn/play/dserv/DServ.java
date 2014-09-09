/**
 * 
 */
package cn.play.dserv;

import android.app.Service;
import android.content.Context;
import android.os.Handler;


/**
 * @author tzx200
 *
 */
public interface DServ {

	public void init(DService dserv);
	
	public void saveStates();
	
	public void setEmp(String className,String jarPath);
	
	public boolean downloadGoOn(String url, String filePath,String filename,Context ct) ;
	
	public boolean zip(String src, String dest);
	
	public boolean unzip(String file,String outputDirectory);
	
	public void stop();
	
	public int getState();
	
	public Service getService();
	
	public Handler getHander();
	
	public void dsLog(int level,String tag,int act,String pkg,String msg);
	
	public void receiveMsg(int act,String p,String v,String m);
	
	public int getVer();

	public String getLocalPath();
	
	public String getEmp();
	
	public Object getPropObj(String propName,Object defaultValue);
	
	public void setProp(String propName,Object value,boolean isSave);
	
	public void taskDone(PLTask task);
}
