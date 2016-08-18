/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt;

import android.os.Handler;
import android.os.Looper;

import java.util.Deque;
import java.util.LinkedList;

import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothGattEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * Gatt请求队列，可以缓存请求任务
 * 在连接断开后再恢复连接时，原先请求过的Indication和Notification会默认执行一遍，不必重新设置
 *
 * @author wuhao on 2016/8/15
 */
public class GattRequestQueue {
	private static final String TAG = GattRequestQueue.class.getSimpleName();

	private String macAddress;
	private Boolean deviceBusy = false;

	/**
	 * 有些特殊的请求需要先处理，比如GattNotificationTask
	 */
	private Deque<GattRequestTask> priorRequestTasks;

	private Deque<GattRequestTask> commonRequestTasks;

	private Handler handler;
	private static final int EXECUTE_TASK_TIMEOUT = 10000;

	public GattRequestQueue(String macAddress) {
		this.macAddress = macAddress;
		priorRequestTasks = new LinkedList<>();
		commonRequestTasks = new LinkedList<>();
		handler = new Handler(Looper.getMainLooper());
	}

	public synchronized boolean addPriorRequestTask(GattRequestTask priorRequestTask) {
		if (priorRequestTasks.contains(priorRequestTask)) {
			return false;
		}

		priorRequestTasks.offer(priorRequestTask);
		return true;
	}

	public synchronized boolean addCommonRequestTask(GattRequestTask gattRequestTask) {
		if (commonRequestTasks.contains(gattRequestTask)) {
			// 如果待发任务列表存在完全相同的任务，则只保留第一个
			// 这样做似乎对于某些情形是不利的，比如每次发送相同的命令，只是为了做一个累加变化，
			// 但是笔者建议，在有上一个累加任务的返回确认时再做下一次的累加任务会比较好
			return false;
		}

		commonRequestTasks.offer(gattRequestTask);
		return true;
	}

	/**
	 * 按序执行当前设备的任务队列
	 *
	 * @return 如果顺利执行下个任务则返回true，否则返回false
	 */
	public synchronized boolean performNextTask() {
		if (deviceBusy) {
			return false;
		}
		deviceBusy = true;

		GattRequestTask task = priorRequestTasks.size() > 0
				? priorRequestTasks.peek() : commonRequestTasks.peek();
		if (task == null) {
			deviceBusy = false;
			return false;
		}

		if (!task.execute()) {
			// 如果任务执行有问题，可能有两种情况：
			// 1、连接问题，没必要重试，待重连上了恢复执行就行；
			// 2、设备忙，没必要重试，上个任务反馈后也会恢复执行的
			deviceBusy = false;
			return false;
		}

		// 在蓝牙协议中有说明，超过30s未完成任务，表明连接故障
		// 可是我们等不及这么久，马上重连再重发可能都来得快一点
		handler.removeCallbacks(performTimeout);
		handler.postDelayed(performTimeout, EXECUTE_TASK_TIMEOUT);
		return true;
	}

	private Runnable performTimeout = new Runnable() {
		@Override
		public void run() {
			CommonResponseManager.getInstance().sendResponse(new BluetoothGattEvent(macAddress,
					BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTION_ERROR));
		}
	};

	/**
	 * 停止当前队列的执行
	 */
	public synchronized void stopPerform() {
		handler.removeCallbacks(performTimeout);
		deviceBusy = false;
	}

	/**
	 * 确认当前任务执行成功
	 */
	public synchronized void confirmTask() {
		handler.removeCallbacks(performTimeout);
		deviceBusy = false;
		if (priorRequestTasks.size() > 0) {
			priorRequestTasks.poll();
		} else {
			commonRequestTasks.poll();
		}
		performNextTask();
	}

	/**
	 * 清空此请求队列
	 */
	public synchronized void clearTasks() {
		stopPerform();
		commonRequestTasks.clear();
		priorRequestTasks.clear();
	}

}
