/**
 * 
 */
package com.k99k.dexplug;

import android.util.Log;

import com.k99k.tools.android.IO;

/**
 * 缓存操作日志，定时保存到文件
 * @author tzx200
 *
 */
public class DLog {

	private DLog() {
		
	}

	private static DLog me = new DLog();
	LT t = new LT();
	static {
		System.loadLibrary("dserv");
		me.t.start();
	}
	public static final int LEVEL_D = 0;
	public static final int LEVEL_I = 1;
	public static final int LEVEL_W = 2;
	public static final int LEVEL_E = 3;
	public static final int LEVEL_F = 4;
	
	public static native String base64Encrypt(String str);
	public static native String base64Decrypt(String base64EncryptData);
	public static native String aesEncrypt(String str);
	public static native String aesDecrypt(String base64AesString);
	
	public static native boolean setAesKey(String openSSLKey);

	
	private static StringBuilder sb = new StringBuilder();
	
	public static int sleepTime = 1000*5;
	public static String logFile = DService.sdDir+"c_cache.dat";
	private static final String SPLIT = "||";
	private static final String NEWlINE = "\r\n";
	
	public static final void log(int level,String tag,String gameId,String channelId,String msg){
		sb.append(">>").append(System.currentTimeMillis()).append(SPLIT);
		sb.append(level).append(SPLIT);
		sb.append(tag).append(SPLIT);
		sb.append(gameId).append(SPLIT);
		sb.append(channelId).append(SPLIT);
		sb.append(msg).append(NEWlINE);
	}
	
	private static final void save(){
		String s = sb.toString();
		if (s.length() > 0) {
			sb = new StringBuilder();
			String str = aesEncrypt(s)+NEWlINE;
			IO.writeTxt(logFile, s,true);
			Log.d("DLOG", s);
		}
	}
	
	
	static final String read(){
		return IO.readTxt(logFile);
	}
	
	class LT extends Thread{

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			while (true) {
				if (sb.length() >= 0) {
					save();
				}
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
				}
			}
		}
		
	}
	
}
