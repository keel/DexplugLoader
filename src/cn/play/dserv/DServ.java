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

	
	public static final int LEVEL_D = 0;
	public static final int LEVEL_I = 1;
	public static final int LEVEL_W = 2;
	public static final int LEVEL_E = 3;
	public static final int LEVEL_F = 4;
	
	public static final String RECEIVER_ACTION = "cn.play.dservice";

	
	static final int STATE_RUNNING = 0;
	static final int STATE_PAUSE = 1;
	static final int STATE_STOP = 2;
	static final int STATE_NEED_RESTART = 3;
	static final int STATE_DIE = 4;
	static final int ACT_EMACTIVITY_START = 11;
	static final int ACT_GAME_INIT = 21;
	static final int ACT_GAME_EXIT = 22;
	static final int ACT_GAME_CONFIRM = 23;
	static final int ACT_GAME_CUSTOM = 24;
	static final int ACT_FEE_INIT = 31;
	static final int ACT_FEE_OK = 32;
	static final int ACT_FEE_FAIL = 33;
	static final int ACT_PUSH_RECEIVE = 41;
	static final int ACT_PUSH_CLICK = 42;
	static final int ACT_APP_INSTALL = 51;
	static final int ACT_APP_REMOVE = 52;
	static final int ACT_BOOT = 61;
	static final int ACT_NET_CHANGE = 62;
	static final int ACT_RECV_INIT = 71;
	static final int ACT_RECV_INITEXIT = 72;
	static final int ACT_OTHER = 80;
	
	void init(DService dserv);
	
	
	void saveStates();
	
	
	void stop();
	
	int getState();
	
	Service getService();
	
	public Handler getHander();
	
	public void log(int level,String tag,String gameId,String channelId,String msg);
	
	public void receiveMsg(int act,String p,String v,String m);
	
	public int getVer();
}
