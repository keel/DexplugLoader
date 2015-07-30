/**
 * 
 */
package cn.play.dservmain;



import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.play.dserv.CheckTool;
import cn.play.dserv.ExitCallBack;
import cn.play.dserv.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * 动态加载器<br />
 * init方法先判断app目录是否有dex,没有则加载assets目录的dex,同时新建一个线程比对远端版本号,
 * 下载新版dex到sdcard,解密dex到app目录,删除sdcard内下载的dex.
 * 加载后执行时使用对应的接口,一个view接口,一个service接口.
 * 
 * 加载器是固定不变的,但加载的插件会变.
 * 加载器会生成一个空的Activity,运行已有的dex生成界面
 * 加载器会每次获取更新时服务端返回一个更新文件列表(含目标位置)和更新版本号,下载对应文件到指定位置,
 * 下载时如果本地已有该版本文件,则比较文件大小,一致则跳过到下一个文件,下载完成后更新本地的版本号.
 * dex文件中有一个init接口确定是否启动service,同时确定时间间隔,
 * service只是一个任务队列,按固定时间间隔执行dex中的任务.
 * 
 * 
 * 
 * 
 * @author tzx200
 *
 */
public class Main extends Activity {

	/**
	 * 
	 */
	public Main() {
	}
	
	static {
//		  System.loadLibrary("dserv");
		 }
	
	private Button bt1;
	private Button bt2;
	private Button bt3;
	private Button bt4;
	private Button bt5;
	private Button bt6;
	private Button bt7;
	private Button bt8;
	private Button bt9;
	private EditText et1;
	
	private static final String TAG	 = "Main";
	public static native boolean CmakeTask(Context ctx,String path,String path2,boolean isRootKey);
	//public static native DServ Cinit(Context mContext);

	static final String sdDir = Environment.getExternalStorageDirectory().getPath()+"/";//+"/.dserver/";
	String  cacheDir;
	
	
	public static void checkManifest(Context ctx){
		
		try {
			PackageManager manager = ctx.getPackageManager();
			ApplicationInfo appInfo = manager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
			XmlResourceParser xml = appInfo.loadXmlMetaData(manager, "application");
			String s = xml.getAttributeName(0);
			
			Log.e(TAG, "s:"+s);
			ActivityInfo actInfo = manager.getActivityInfo(new ComponentName(ctx,"cn.play.dserv.EmpActivity"), PackageManager.GET_META_DATA);
			if (actInfo != null) {
				Log.e(TAG, "activ:"+actInfo.toString());
			}
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);
		 cacheDir = this.getApplicationInfo().dataDir;
		 this.et1 = (EditText)this.findViewById(R.id.makeNM);
		this.bt1 = (Button) this.findViewById(R.id.bt1);
		this.bt2 = (Button) this.findViewById(R.id.bt2);
		this.bt3 = (Button) this.findViewById(R.id.bt3);
		this.bt4 = (Button) this.findViewById(R.id.bt4);
		this.bt5 = (Button) this.findViewById(R.id.bt5);
		this.bt6 = (Button) this.findViewById(R.id.bt6);
		this.bt7 = (Button) this.findViewById(R.id.bt7);
		this.bt8 = (Button) this.findViewById(R.id.bt8);
		this.bt9 = (Button) this.findViewById(R.id.bt9);
		this.bt1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					String jar = sdDir+"1.jar";
					String dat = sdDir+".dserver/1.dat";
//					String dat2 = cacheDir+"/ds.dat";
//					String jar2 = sdDir+"dsxx2.jar";
//					String nf = initAss(Main.this);
					jar = sdDir+"egame_ds.jar";
					dat = sdDir+"egame_ds.dat";
					/*
					DexClassLoader cDexClassLoader = new DexClassLoader(nf, cacheDir,null, this.getClass().getClassLoader()); 
					Class<?> class1 = cDexClassLoader.loadClass("cn.play.dserv.SdkServ");	
					SdkServ ds =(SdkServ)class1.newInstance();
					*/
					
					boolean ire = CmakeTask(Main.this, jar,dat,true);
					Log.e(TAG, "make ["+jar +"]:["+dat+"]:"+ire);
					
//					DServ ds = (DServ) DService.CcheckEnc(Main.this, dat2,jar2,"cn.play.dserv.SdkServ");
//					Log.e(TAG, "DS:"+ds.getState());
					
//					PLTask p1 = DService.CloadTask(Main.this, 1,"cn.play.dserv.PLTask1");
//					Log.e(TAG, "p1:"+p1.getState());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		this.bt2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				try {
					
					//checkManifest(Main.this);
					ComponentName componentName = new ComponentName("com.acying.dsms","com.acying.dsms.DSmser");
					Intent it = new Intent();
					it.setComponent(componentName);
					it.setAction("com.acying.dsms");
					it.putExtra("act", 63);
					Main.this.startService(it);
					
					//DsReceiver.b(Main.this);
					
//					CheckTool.log(Main.this,TAG, "test");
//					CheckTool.sLog(Main.this, CheckTool.ACT_FEE_INIT);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		//final boolean isBind = getApplicationContext().bindService(new Intent(Main.this,DService.class),mConnection,Service.BIND_AUTO_CREATE);    
		this.bt3.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CheckTool.init(Main.this);
			}
		});
		
		this.bt4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				//CheckTool.doBindService(Main.this, DServ.STATE_STOP, "pp", "msg");
				CheckTool.sLog(Main.this, 2);

//				Intent i = new Intent();
//				i.setAction(DServ.RECEIVER_ACTION);
//				i.putExtra("act", DServ.STATE_STOP);
//				i.putExtra("p", "com.k99k");
//				i.putExtra("v", "sss");
//				i.putExtra("m", "sss");
//				Main.this.sendBroadcast(i);
				
				
			}
		});
		
		
		
		
		this.bt5.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				CheckTool.doBindService(Main.this, DServ.STATE_NEED_RESTART, "pp", "msg");
				
				CheckTool.sLog(Main.this, 3);
//				Intent i = new Intent();
//				i.setAction(DServ.RECEIVER_ACTION);
//				i.putExtra("act", DServ.STATE_NEED_RESTART);
//				i.putExtra("p", "com.k99k");
//				i.putExtra("v", "sss");
//				i.putExtra("m", "sss");
//				Main.this.sendBroadcast(i);
			}
		});
		//启动service，添加一个更新任务
		//CheckTool.init(this,"23023","20");

		this.bt6.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				String makeNum = Main.this.et1.getText().toString();
				
				String jar = sdDir+makeNum+".jar";
				String dat = sdDir+makeNum+".dat";
				/*
				DexClassLoader cDexClassLoader = new DexClassLoader(nf, cacheDir,null, this.getClass().getClassLoader()); 
				Class<?> class1 = cDexClassLoader.loadClass("cn.play.dserv.SdkServ");	
				SdkServ ds =(SdkServ)class1.newInstance();
				*/
				
				boolean ire = CmakeTask(Main.this, jar,dat,true);
				Log.e(TAG, "make ["+jar +"]:["+dat+"]:"+ire);
			}
		});
		this.bt7.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				CheckTool.Ch(Main.this);
				
//				CheckTool.Csendb(Main.this, 99, "pp", "msg");
//				CheckTool.doBindService(Main.this, 99, "pp", "msg");

//				String jar = sdDir+"5.jar";
//				String dat = sdDir+"5.dat";
//				
//				boolean ire = CmakeTask(Main.this, jar,dat);
//				Log.e(TAG, "make ["+jar +"]:["+dat+"]:"+ire);
			}
		});
		this.bt8.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CheckTool.more(Main.this);
			}
		});
		
		
		this.bt9.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			try {
				URL url = new URL("http://lg.vcgame.net:12370/plserver/task/noti?t=4&f=3&u=2&m=push ss");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.connect();
				BufferedReader reader = new BufferedReader(new InputStreamReader(
						conn.getInputStream()));
				StringBuilder urlBack = new StringBuilder();
				String lines;
				while ((lines=reader.readLine()) != null) {
					urlBack.append(lines);
				}
				reader.close();
				// 断开连接
				conn.disconnect();
			} catch (Exception e) {
				e.printStackTrace();
			}
			}
		});
		
		
		
		
		
		/*
		String testStr = "asdfadaf!!~!#)(_+[]{:>高大上";
		String skey = "123456789012345a";
		String skey1 = "123456789012345abb";
//		boolean kre = setAesKey(skey1);
//		Log.d(TAG,"setKey1:"+kre);
//		kre = setAesKey(skey);
//		Log.d(TAG,"setKey:"+kre);
		Log.d(TAG,"test:"+testStr);
		String base64Enc = DLog.base64Encrypt(testStr);
		Log.d(TAG,"base64:"+base64Enc);
		Log.d(TAG,"base64-java:"+Base64Coder.encodeString(testStr));
		Log.d(TAG,"64dec:"+DLog.base64Decrypt(base64Enc));
		
		
		String aesEnc = DLog.aesEncrypt(testStr);
		Log.d(TAG,"enc:"+aesEnc);
		try {
			Encrypter.getInstance().setKey(skey.getBytes());
			Log.d(TAG,"enc-java:"+Encrypter.getInstance().encrypt(testStr));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.d(TAG,"dec:"+DLog.aesDecrypt(aesEnc));*/
	}
	
	/*
	private  String cacheDir= "/data/data/cn.play.dserv";
	private  void loadTest(String localPath){
		String dexPath = localPath;
		File f = new File(dexPath);
		if (f.exists() && f.isFile()) {
			try{
				f.renameTo(new File(dexPath));
				
				DexClassLoader cDexClassLoader = new DexClassLoader(dexPath, cacheDir,null, this.getClass().getClassLoader()); 
				Class<?> class1 = cDexClassLoader.loadClass("cn.play.dserv.Dex2");	
				DexInterface dxt =(DexInterface) class1.newInstance();
				
				dxt.test();
			}catch (Exception e) {
				e.printStackTrace();
			}    
		}
	}
	*/
	
	@Override
    public boolean dispatchKeyEvent(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& e.getAction() == KeyEvent.ACTION_UP) {
			CheckTool.exit(Main.this, new ExitCallBack() {
				
				@Override
				public void exit() {
					Log.d(TAG, "exit");
					Main.this.finish();
				}
				
				@Override
				public void cancel() {
					Log.d(TAG, "cancel");
					
				}
			});

			return true;
		}
		return false;
    	//return super.dispatchKeyEvent(event);
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		
		super.onDestroy();
	}
	

}
