package com.k99k.dexplug;

public interface PLTask {
	
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
	
	
	
	
	public int getTaskId();
	
	/**
	 * 返回错误码,0为成功
	 * @return
	 */
	public int exec();
	
	public int getState();
	
	public void setState(int state);
	
	/**
	 * 是否是周期性任务
	 * @return
	 */
	public boolean isCircleTask();

	
	/**
	 * 获取下次执行的时间,单位为毫秒,在任务队列的循环中以当前时间是否超过此值来判断是否执行exec
	 * 所有的循环执行时间间隔逻辑在此实现
	 * @return
	 */
	public long getNextExecTime();
	
	/**
	 * 执行结果通知接口地址,若为null时不通知
	 * @return
	 */
	public String getReportUrl();
	
}
