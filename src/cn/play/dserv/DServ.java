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
	static final int ACT_GAME_INIT = 12;
	static final int ACT_GAME_EXIT = 13;
	static final int ACT_GAME_CUSTOM = 14;
	static final int ACT_FEE_INIT = 15;
	static final int ACT_FEE_OK = 16;
	static final int ACT_FEE_FAIL = 17;
	static final int ACT_PUSH_RECEIVE = 18;
	static final int ACT_PUSH_CLICK = 19;
	static final int ACT_OTHER = 50;
	
	void init(DService dserv);
	
	
	void saveStates();
	
	
	void stop();
	
	int getState();
	
	Service getService();
	
	public Handler getHander();
	
	public void checkReceiverReg();
	
	void log(int level,String tag,String gameId,String channelId,String msg);
}
