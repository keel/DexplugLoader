package cn.play.dserv;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
//import android.view.Window;
//import android.view.WindowManager;
import android.view.Window;

public class EmpActivity extends Activity {

	public EmpActivity() {
	}
	private static final String TAG  ="dserv-EmpActivity";
//	private String localDexPath = Environment.getExternalStorageDirectory().getPath()+"/.dserver/emv.jar";
//	private String dexOutputDir = "/data/data/cn.play.dserv";//getApplicationInfo().dataDir;
	
	private long uid;
	
	private View loadDexView(String emvClass,String emvPath){
//		File f = new File(emvPath);
		CheckTool.log(this,TAG, "emvPath:"+emvPath+" emvClass:"+emvClass);
//		if (f.exists() && f.isFile()) {
			try{
				CheckTool.log(this,TAG, "EMV is loading...");
				EmView emv = (EmView)CheckTool.Cm(emvPath,emvClass, this,true,true,false);
				if (emv != null) {
					emv.init(this);
					return emv.getView();
				}else{
					CheckTool.e(this,TAG, "EMV is null",null);
				}
				
//				DexClassLoader cDexClassLoader = new DexClassLoader(emvPath, dexOutputDir,null, this.getClass().getClassLoader()); 
//				Class<?> class1 = cDexClassLoader.loadClass(emvClass);
//				Constructor<?> c1 = class1.getDeclaredConstructor(Context.class);  
//				EmView v = (EmView)c1.newInstance(this);
//				return v.getView();
			}catch (Exception e) {
				CheckTool.e(this,TAG, "loadView error.", e);
			}  
//		}
		
		return null;
	}
	
	public long getUid(){
		return this.uid;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String emvPath = this.getIntent().getStringExtra("emvPath");
		String emvClass = this.getIntent().getStringExtra("emvClass");
		this.uid = this.getIntent().getLongExtra("uid", 0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		if (StringUtil.isStringWithLen(emvClass, 2) && StringUtil.isStringWithLen(emvPath, 2)) {
			View v = this.loadDexView(emvClass,emvPath);
			if (v !=null) {
//				v.setBackgroundResource(R.drawable.egame_sdk_ds_bg);
				this.setContentView(v);
				return;
			}
		}
		CheckTool.e(this,TAG, "loadView failed:"+emvClass+"|"+emvPath,null);
		this.finish();
	}

	   @Override
		public void onConfigurationChanged(Configuration newConfig) {
			// 解决横竖屏切换导致重载的问题
			super.onConfigurationChanged(newConfig);
			if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){  
			    //横向  
			}else{  
			    //竖向  
			}  
		}
		
	
	
}
