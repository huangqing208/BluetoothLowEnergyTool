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
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;


/**
 * 此类用于适时返回事件给需要关注的对象
 *
 * @author wuhao on 2016/7/15
 */
public class CommonResponseManager {
	private static final String TAG = CommonResponseManager.class.getSimpleName();

	private List<CommonResponseCallback> uiCallbacks;
	private List<CommonResponseCallback> taskCallbacks;

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

	private boolean notifyUI = false;

	public synchronized void setUINotification(boolean notification) {
		notifyUI = notification;
	}

	public boolean sendResponse(final CommonResponseEvent commonResponseEvent) {
		Log.i(TAG, commonResponseEvent.toString());
		synchronized (taskCallbacks) {
			for (int i = 0; i < taskCallbacks.size(); ++i) {
				taskCallbacks.get(i).onCommonResponded(commonResponseEvent.clone());
			}
		}
		synchronized (this) {
			if (notifyUI) {
				synchronized (uiCallbacks) {
					final int size = uiCallbacks.size();
					if (size > 0) {
						// UI接收的话必须在主线程，如果是业务逻辑的话，无所谓
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								// UICallback只提醒最后一个
								uiCallbacks.get(size - 1).onCommonResponded(commonResponseEvent);
							}
						});
					}
				}
			}
		}
		return taskCallbacks.size() > 0 || uiCallbacks.size() > 0;
	}

}
