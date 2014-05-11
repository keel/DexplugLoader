package com.k99k.dexplug;

import java.io.File;
import java.lang.reflect.Constructor;

import dalvik.system.DexClassLoader;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class EmptyActivity extends Activity {

	public EmptyActivity() {
	}
	private static final String TAG  ="EmptyActivity";
	private String localDexPath = Environment.getExternalStorageDirectory().getPath()+"/.dserver/emv.jar";
	private String dexOutputDir = "/data/data/com.k99k.dexplug";//getApplicationInfo().dataDir;
	
	private View loadDexView(){
		File f = new File(this.localDexPath);
		if (f.exists() && f.isFile()) {
			try{
				DexClassLoader cDexClassLoader = new DexClassLoader(this.localDexPath, dexOutputDir,null, this.getClass().getClassLoader()); 
				Class<?> class1 = cDexClassLoader.loadClass("com.k99k.dexplug.MoreView");
				Constructor c1 = class1.getDeclaredConstructor(Context.class);  
//				EmView v =(EmView)class1.newInstance();
				EmView v = (EmView)c1.newInstance(this);
				return v.getView();
			}catch (Exception e) {
				Log.e(TAG, "loadDexView error.",e);
			}  
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View v = this.loadDexView();
		if (v !=null) {
			this.setContentView(this.loadDexView());
		}else{
			Log.e(TAG, "loadDexView failed.");
			this.finish();
		}
	}

	
	
	
}
