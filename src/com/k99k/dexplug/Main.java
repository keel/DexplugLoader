/**
 * 
 */
package com.k99k.dexplug;


import dalvik.system.DexClassLoader;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

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
	
	

	private LinearLayout area;
	private Button bt1;
	private Button bt2;
	
	private static String dexPath = "/mnt/sdcard/plug1.jar";
	private static String dexOutputDir= "/data/data/com.k99k.dexplug";
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main);
		this.bt1 = (Button) this.findViewById(R.id.bt1);
		this.bt1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				Intent i = new Intent();  
				i.setClass(Main.this, EmptyActivity.class);  
				Main.this.startActivity(i);

			}
		});
		
		this.bt2 = (Button) this.findViewById(R.id.bt2);
		this.bt2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

			}
		});
		
//		DexClassLoader cDexClassLoader = new DexClassLoader(dexPath, dexOutputDir,null, this.getClass().getClassLoader()); 
//		try{
//			Class<?> class1 = cDexClassLoader.loadClass("com.k99k.dexplug.PlugContent");	
//			PlugInterface plug =( PlugInterface)class1.newInstance();
//			this.setContentView(plug.plugView(this, null, null));
//		}catch (Exception e) {    
//			e.printStackTrace();
//		}    
		//(new File(downPath)).mkdirs();
		
		//启动service，添加一个更新任务
		Intent i = new Intent();  
		i.setClass(this, DService.class);  
		this.startService(i);
	}
	
	void plugIn(Context cx){

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
