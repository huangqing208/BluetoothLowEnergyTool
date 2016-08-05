/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.manager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.bit.hao.ble.tool.response.callbacks.CommonResponseCallback;
import cn.bit.hao.ble.tool.response.events.BluetoothLeScanEvent;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;


/**
 * 此类用于适时返回事件给需要关注的对象
 *
 * @author wuhao on 2016/7/15
 */
public class CommonResponseManager {
	private static final String TAG = CommonResponseManager.class.getSimpleName();

	private final List<CommonResponseCallback> uiCallbacks;
	private final List<CommonResponseCallback> taskCallbacks;

	private boolean notifyUI = false;

	private Handler mHandler;

	private CommonResponseManager() {
		mHandler = new Handler(Looper.getMainLooper());
		uiCallbacks = new ArrayList<>();
		taskCallbacks = new ArrayList<>();
	}

	private static CommonResponseManager instance;

	public static synchronized CommonResponseManager getInstance() {
		if (instance == null) {
			instance = new CommonResponseManager();
		}
		return instance;
	}

	/**
	 * 指定一个被通知的回调对象，按照添加时顺序，在此对象添加之后添加的回调对象会被删除，即不再接收回调。
	 * 如果commonResponseCallback为null的话，表示禁用UI回调功能。
	 *
	 * @param commonResponseCallback 需要通知到的UI回调对象
	 */
	public void setUINotification(CommonResponseCallback commonResponseCallback) {
		synchronized (uiCallbacks) {
			if (!uiCallbacks.contains(commonResponseCallback)) {
				return;
			}
			notifyUI = commonResponseCallback != null;
			if (!notifyUI) {
				return;
			}
			for (int i = uiCallbacks.size() - 1; i >= 0; --i) {
				if (uiCallbacks.get(i).equals(commonResponseCallback)) {
					break;
				} else {
					uiCallbacks.remove(i);
				}
			}
		}
	}

	public boolean addUICallback(CommonResponseCallback callback) {
		synchronized (uiCallbacks) {
			if (uiCallbacks.contains(callback)) {
				return false;
			}
			uiCallbacks.add(callback);
			return true;
		}
	}

	public void removeUICallback(CommonResponseCallback callback) {
		synchronized (uiCallbacks) {
			uiCallbacks.remove(callback);
		}
	}

	public boolean addTaskCallback(CommonResponseCallback callback) {
		synchronized (taskCallbacks) {
			if (taskCallbacks.contains(callback)) {
				return false;
			}
			taskCallbacks.add(callback);
			return true;
		}
	}

	public void removeTaskCallback(CommonResponseCallback callback) {
		synchronized (taskCallbacks) {
			taskCallbacks.remove(callback);
		}
	}

	/**
	 * 此方法可以将消息会调给目标对象
	 *
	 * @param commonResponseEvent 被通知到的消息
	 */
	public void sendResponse(final CommonResponseEvent commonResponseEvent) {
		if (!(commonResponseEvent instanceof BluetoothLeScanEvent)) {
			Log.i(TAG, commonResponseEvent.toString());
		}
		synchronized (taskCallbacks) {
			for (int i = 0; i < taskCallbacks.size(); ++i) {
				taskCallbacks.get(i).onCommonResponded(commonResponseEvent.clone());
			}
		}
		// UI接收的话必须在主线程，如果是业务逻辑的话，无所谓
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (uiCallbacks) {
					if (!notifyUI) {
						return;
					}
					int size = uiCallbacks.size();
					if (size > 0) {
						// UICallback只提醒最后一个
						uiCallbacks.get(size - 1).onCommonResponded(commonResponseEvent);
					}
				}
			}
		});
	}

}
