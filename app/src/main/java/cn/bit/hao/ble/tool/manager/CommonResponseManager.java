/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.manager;

import java.util.ArrayList;
import java.util.List;

import cn.bit.hao.ble.tool.callbacks.CommonResponseCallback;
import cn.bit.hao.ble.tool.events.ResponseEvent;


/**
 * 此类用于适时返回事件给需要关注的对象
 *
 * @author wuhao on 2016/7/15
 */
public class CommonResponseManager {

	private List<CommonResponseCallback> uiCallbacks;
	private List<CommonResponseCallback> taskCallbacks;

	private CommonResponseManager() {
		uiCallbacks = new ArrayList<>();
		taskCallbacks = new ArrayList<>();
	}

	private static CommonResponseManager instance = new CommonResponseManager();

	public static CommonResponseManager getInstance() {
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

	public void removeAllCallbacks() {
		taskCallbacks.clear();
		uiCallbacks.clear();
	}

	private boolean notifyUI = false;

	public void setUINotification(boolean notification) {
		notifyUI = notification;
	}

	public boolean sendResponse(ResponseEvent responseEvent) {
		boolean sendout = false;
		synchronized (taskCallbacks) {
			for (int i = 0; i < taskCallbacks.size(); ++i) {
				taskCallbacks.get(i).onCommonResponded(responseEvent.clone());
				sendout = true;
			}
		}
		/**
		 * UICallback只提醒最后一个
		 */
		if (notifyUI) {
			synchronized (uiCallbacks) {
				int size = uiCallbacks.size();
				if (size > 0) {
					uiCallbacks.get(size - 1).onCommonResponded(responseEvent);
					sendout = true;
				}
			}
		}
		return sendout;
	}

}
