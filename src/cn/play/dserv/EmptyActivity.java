package cn.play.dserv;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class EmptyActivity extends Activity {

	public EmptyActivity() {
	}
	private static final String TAG  ="EmptyActivity";
//	private String localDexPath = Environment.getExternalStorageDirectory().getPath()+"/.dserver/emv.jar";
//	private String dexOutputDir = "/data/data/cn.play.dserv";//getApplicationInfo().dataDir;
	
	private View loadDexView(String emvClass,String emvPath){
//		File f = new File(emvPath);
		Log.d(TAG, "emvPath:"+emvPath+" emvClass:"+emvClass);
//		if (f.exists() && f.isFile()) {
			try{
				Log.e(TAG, "EMV is loading...");
				EmView emv = (EmView)CheckTool.Cload(emvPath,emvClass, this,true,true);
				if (emv != null) {
					emv.init(this);
					return emv.getView();
				}else{
					Log.e(TAG, "EMV is null");
					
				}
				
//				DexClassLoader cDexClassLoader = new DexClassLoader(emvPath, dexOutputDir,null, this.getClass().getClassLoader()); 
//				Class<?> class1 = cDexClassLoader.loadClass(emvClass);
//				Constructor<?> c1 = class1.getDeclaredConstructor(Context.class);  
//				EmView v = (EmView)c1.newInstance(this);
//				return v.getView();
			}catch (Exception e) {
				Log.e(TAG, "loadDexView error.",e);
			}  
//		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String emvPath = this.getIntent().getStringExtra("emvPath");
		String emvClass = this.getIntent().getStringExtra("emvClass");
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if (StringUtil.isStringWithLen(emvClass, 2) && StringUtil.isStringWithLen(emvPath, 2)) {
			View v = this.loadDexView(emvClass,emvPath);
			if (v !=null) {
				this.setContentView(v);
				return;
			}
		}
		Log.e(TAG, "loadDexView failed:"+emvClass+"|"+emvPath);
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
