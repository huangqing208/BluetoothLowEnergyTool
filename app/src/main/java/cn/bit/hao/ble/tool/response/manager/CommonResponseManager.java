/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.manager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.bit.hao.ble.tool.response.callbacks.CommonResponseListener;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothLeScanResultEvent;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;


/**
 * 此类用于适时返回事件给需要关注的对象
 *
 * @author wuhao on 2016/7/15
 */
public class CommonResponseManager {
	private static final String TAG = CommonResponseManager.class.getSimpleName();

	private final List<CommonResponseListener> uiCallbacks;
	private final List<CommonResponseListener> taskCallbacks;

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
	 * 指定一个被通知的UI回调对象，按照添加时顺序，在此对象添加之后添加的回调对象会被删除，即不再接收回调。
	 *
	 * @param uiCallback 需要通知到的UI回调对象
	 */
	public void registerUINotification(CommonResponseListener uiCallback) {
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
	 * 指定一个UI回调对象，取消对此回调的Notification返回。
	 *
	 * @param uiCallback 指定要取消通知的UI回调对象
	 */
	public void unregisterUINotification(CommonResponseListener uiCallback) {
		synchronized (uiCallbacks) {
			if (uiCallback == null) {
				return;
			}
			notifyUI = false;
			int size = uiCallbacks.size();
			if (size > 0 && uiCallbacks.get(size - 1).equals(uiCallback)) {
				uiCallbacks.remove(size - 1);
			}
		}
	}

	public boolean addUICallback(CommonResponseListener callback) {
		synchronized (uiCallbacks) {
			if (uiCallbacks.contains(callback)) {
				return false;
			}
			uiCallbacks.add(callback);
			return true;
		}
	}

	public void removeUICallback(CommonResponseListener callback) {
		synchronized (uiCallbacks) {
			uiCallbacks.remove(callback);
		}
	}

	public boolean addTaskCallback(CommonResponseListener callback) {
		synchronized (taskCallbacks) {
			if (taskCallbacks.contains(callback)) {
				return false;
			}
			taskCallbacks.add(callback);
			return true;
		}
	}

	public void removeTaskCallback(CommonResponseListener callback) {
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
		if (!(commonResponseEvent instanceof BluetoothLeScanResultEvent)) {
			Log.i(TAG, commonResponseEvent.toString());
		}
		synchronized (taskCallbacks) {
			for (int i = 0; i < taskCallbacks.size(); ++i) {
				// TODO: 通常来说不会修改通知内容的，所以默认用同一个通知对象，必要的话，可以深复制
				taskCallbacks.get(i).onCommonResponded(commonResponseEvent);
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
						uiCallbacks.get(size - 1).onCommonResponded(commonResponseEvent);
					}
				}
			}
		});
	}

}
