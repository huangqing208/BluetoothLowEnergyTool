/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.manager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.bit.hao.ble.tool.response.callbacks.CommonEventListener;
import cn.bit.hao.ble.tool.response.events.CommonEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothLeScanResultEvent;


/**
 * 此类用于适时返回事件给需要关注的对象
 *
 * @author wuhao on 2016/7/15
 */
public class CommonEventManager {
	private static final String TAG = CommonEventManager.class.getSimpleName();

	private final List<CommonEventListener> uiCallbacks;
	private final List<CommonEventListener> taskCallbacks;

	private boolean notifyUI = false;

	private Handler mHandler;

	private CommonEventManager() {
		mHandler = new Handler(Looper.getMainLooper());
		uiCallbacks = new ArrayList<>();
		taskCallbacks = new ArrayList<>();
	}

	private static CommonEventManager instance;

	public static synchronized CommonEventManager getInstance() {
		if (instance == null) {
			instance = new CommonEventManager();
		}
		return instance;
	}

	public boolean isUINotified() {
		return notifyUI;
	}

	/**
	 * 指定一个被通知的UI回调对象，按照添加时顺序，在此对象添加之后添加的回调对象会被删除，即不再接收回调。
	 *
	 * @param uiCallback 需要通知到的UI回调对象
	 */
	public void registerUINotification(CommonEventListener uiCallback) {
		synchronized (uiCallbacks) {
			if (uiCallback == null || !uiCallbacks.contains(uiCallback)) {
				return;
			}
			notifyUI = true;
			for (int i = uiCallbacks.size() - 1; i >= 0; --i) {
				if (uiCallbacks.get(i).equals(uiCallback)) {
					break;
				} else {
					uiCallbacks.remove(i);
				}
			}
		}
	}

	/**
	 * 取消对UI的回调返回。
	 *
	 * @param uiCallback 取消对指定UI回调的返回
	 */
	public void unregisterUINotification(CommonEventListener uiCallback) {
		synchronized (uiCallbacks) {
			if (uiCallback == null) {
				return;
			}
			int size = uiCallbacks.size();
			if (size > 0 && uiCallbacks.get(size - 1).equals(uiCallback)) {
				notifyUI = false;
			}
		}
	}

	public boolean addUICallback(CommonEventListener callback) {
		synchronized (uiCallbacks) {
			if (uiCallbacks.contains(callback)) {
				return false;
			}
			uiCallbacks.add(callback);
			return true;
		}
	}

	public void removeUICallback(CommonEventListener callback) {
		synchronized (uiCallbacks) {
			uiCallbacks.remove(callback);
		}
	}

	public boolean addTaskCallback(CommonEventListener callback) {
		synchronized (taskCallbacks) {
			if (taskCallbacks.contains(callback)) {
				return false;
			}
			taskCallbacks.add(callback);
			return true;
		}
	}

	public void removeTaskCallback(CommonEventListener callback) {
		synchronized (taskCallbacks) {
			taskCallbacks.remove(callback);
		}
	}

	/**
	 * 此方法可以将消息会调给目标对象
	 *
	 * @param commonEvent 被通知到的消息
	 */
	public void sendResponse(final CommonEvent commonEvent) {
		if (!(commonEvent instanceof BluetoothLeScanResultEvent)) {
			Log.i(TAG, commonEvent.toString());
		}
		synchronized (taskCallbacks) {
			for (int i = 0; i < taskCallbacks.size(); ++i) {
				// TODO: 通常来说不会修改通知内容的，所以默认用同一个通知对象，必要的话，可以深复制
				taskCallbacks.get(i).onCommonResponded(commonEvent);
//				taskCallbacks.get(i).onCommonResponded(commonResponseEvent.clone());
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
						uiCallbacks.get(size - 1).onCommonResponded(commonEvent);
					}
				}
			}
		});
	}

}
