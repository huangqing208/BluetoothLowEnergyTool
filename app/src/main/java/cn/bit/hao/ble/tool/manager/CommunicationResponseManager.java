/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.manager;

import java.util.ArrayList;
import java.util.List;

import cn.bit.hao.ble.tool.interfaces.CommunicationResponseCallback;


/**
 * @author wuhao on 2016/7/15
 */
public class CommunicationResponseManager {

	private List<CommunicationResponseCallback> uiCallbacks;
	private List<CommunicationResponseCallback> taskCallbacks;

	private CommunicationResponseManager() {
		uiCallbacks = new ArrayList<>();
		taskCallbacks = new ArrayList<>();
	}

	private static CommunicationResponseManager instance = new CommunicationResponseManager();

	public static CommunicationResponseManager getInstance() {
		return instance;
	}

	public boolean addUICallback(CommunicationResponseCallback callback) {
		if (uiCallbacks.contains(callback)) {
			return false;
		}
		uiCallbacks.add(callback);
		return true;
	}

	public void removeUICallback(CommunicationResponseCallback callback) {
		uiCallbacks.remove(callback);
	}

	public boolean addTaskCallback(CommunicationResponseCallback callback) {
		if (taskCallbacks.contains(callback)) {
			return false;
		}
		taskCallbacks.add(callback);
		return true;
	}

	public void removeTaskCallback(CommunicationResponseCallback callback) {
		taskCallbacks.remove(callback);
	}

	private boolean notifyUI = true;

	public void setUINotification(boolean notification) {
		notifyUI = notification;
	}

	public void sendResponse(int filedCode) {
		for (int i = 0; i < taskCallbacks.size(); ++i) {
			taskCallbacks.get(i).onCommunicationResponded(filedCode);
		}
		/**
		 * UICallback只提醒最后一个
		 */
		if (notifyUI) {
			int size = uiCallbacks.size();
			if (size > 0) {
				uiCallbacks.get(size - 1).onCommunicationResponded(filedCode);
			}
		}
	}

}
