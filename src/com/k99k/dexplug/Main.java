/**
 * 
 */
package com.k99k.dexplug;

import dalvik.system.DexClassLoader;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * @author tzx200
 *
 */
public class Main extends Activity {

	/**
	 * 
	 */
	public Main() {
	}

	private LinearLayout area;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);
		Button bt = (Button)this.findViewById(R.id.bt1);
		this.area = (LinearLayout) this.findViewById(R.id.showPlug);
		
		
		bt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Main.this.plugIn(Main.this);
			}
		});
		
	}
	
	void plugIn(Context cx){

		String dexPath = "/mnt/sdcard/plug1.jar";
		String dexOutputDir= "/data/data/com.k99k.dexplug";
		
		DexClassLoader cDexClassLoader = new DexClassLoader(dexPath, dexOutputDir,null, this.getClass().getClassLoader()); 
		try{
			Class<?> class1 = cDexClassLoader.loadClass("com.k99k.dexplug.PlugContent");	
			PlugInterface plug =( PlugInterface)class1.newInstance();
			this.area.addView(plug.plugView(cx, null, null));
		}catch (Exception e) {    
			e.printStackTrace();
		}    

	}
	
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
