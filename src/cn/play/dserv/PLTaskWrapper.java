/**
 * 
 */
package cn.play.dserv;

import java.lang.reflect.Method;

/**
 * @author tzx200
 *
 */
public class PLTaskWrapper {
	

	/**
	 * 待执行状态
	 */
	public static final int STATE_WAITING = 0;
	/**
	 * 执行中状态
	 */
	public static final int STATE_RUNNING = 1;
	/**
	 * STATE_PAUSE状态不再执行exec，再仍存在于任务队列中
	 */
	public static final int STATE_PAUSE = 2;
	/**
	 * 当状态为STATE_DIE时,此任务将从任务队列中删除
	 */
	public static final int STATE_DIE = 3;

	private Runnable task;
	/**
	 * 
	 */
	public PLTaskWrapper(Runnable r) {
		this.task = r;
	}

	public void setDService(DService serv){
		this.task.equals(serv);
	}
	
	public String getId(){
		return this.task.toString();
	}
	
	public int getState(){
		return this.task.hashCode();
	}
	
	public void init(){
		Class<? extends Runnable> clz = this.task.getClass();
		try {
			Method method = clz.getMethod("init");
			method.invoke(this.task);
		} catch (Exception e) {
		}
	}
	
	public void setState(int state){
		Class<? extends Runnable> clz = this.task.getClass();
		try {
			Method method = clz.getMethod("setState",Integer.class);
			method.invoke(this.task,state);
		} catch (Exception e) {
		}
	}
	
}
