/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.manager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import cn.bit.hao.ble.tool.events.BluetoothEvent;
import cn.bit.hao.ble.tool.interfaces.BluetoothCallback;
import cn.bit.hao.ble.tool.utils.BluetoothUtil;

/**
 * 此Manager的最大作用就是管理蓝牙状态，并且向需要接收状态变化的对象通知状态变化事件
 *
 * @author wuhao on 2016/7/16
 */
public class BluetoothStateManager {
	private static final String TAG = BluetoothStateManager.class.getSimpleName();

	private BluetoothStateManager() {
		uiCallbacks = new ArrayList<>();
		taskCallbacks = new ArrayList<>();
	}

	private static BluetoothStateManager instance;

	public static synchronized BluetoothStateManager getInstance(Context context) {
		if (instance == null) {
			instance = new BluetoothStateManager();
			instance.initBluetoothState(context);
		}
		return instance;
	}

	/**
	 * 初始化BluetoothStateManager
	 *
	 * @param context 上下文对象
	 * @return 如果设备不支持蓝牙，则返回false
	 */
	private boolean initBluetoothState(Context context) {
		BluetoothAdapter bluetoothAdapter = BluetoothUtil.getBluetoothAdapter(context);
		if (bluetoothAdapter == null) {
			// Toast.makeText(SystemStateService.this, "Bluetooth not supported!", Toast.LENGTH_SHORT).show();
			this.bluetoothSupported = false;
			return false;
		}
		setBluetoothState(bluetoothAdapter.getState());
		return true;
	}

	/**
	 * 默认设备支持蓝牙
	 */
	private boolean bluetoothSupported = true;

	private int bluetoothState = BluetoothAdapter.ERROR;

	private List<BluetoothCallback> uiCallbacks;
	private List<BluetoothCallback> taskCallbacks;

	/**
	 * <p>添加一个UI回调，不会添加重复项，如果尝试添加重复项则返回false</p>
	 *
	 * @param callback 被添加的UI回调
	 * @return 如果添加成功则返回true
	 */
	public boolean addUICallback(BluetoothCallback callback) {
		if (!bluetoothSupported) {
			return false;
		}
		synchronized (uiCallbacks) {
			if (uiCallbacks.contains(callback)) {
				return false;
			}
			uiCallbacks.add(callback);
			return true;
		}
	}

	/**
	 * 删除UI回调
	 *
	 * @param callback 被删除的UI回调
	 */
	public void removeUICallback(BluetoothCallback callback) {
		synchronized (uiCallbacks) {
			uiCallbacks.remove(callback);
		}
	}

	/**
	 * <p>添加一个UI回调，不会添加重复项，如果尝试添加重复项则返回false</p>
	 *
	 * @param callback 被添加的UI回调
	 * @return 如果添加成功则返回true
	 */
	public boolean addTaskCallback(BluetoothCallback callback) {
		if (!bluetoothSupported) {
			return false;
		}
		synchronized (taskCallbacks) {
			if (taskCallbacks.contains(callback)) {
				return false;
			}
			taskCallbacks.add(callback);
			return true;
		}
	}

	/**
	 * 删除UI回调
	 *
	 * @param callback 被删除的UI回调
	 */
	public void removeTaskCallback(BluetoothCallback callback) {
		synchronized (taskCallbacks) {
			taskCallbacks.remove(callback);
		}
	}

	/**
	 * 通过回调通知事件
	 *
	 * @param event 被通知的事件
	 */
	private void sendActionCode(BluetoothEvent event) {
		synchronized (taskCallbacks) {
			for (int i = 0; i < taskCallbacks.size(); ++i) {
				taskCallbacks.get(i).onBluetoothActionHappened(new BluetoothEvent(event));
			}
		}
		/**
		 * UICallback只提醒最后一个
		 */
		synchronized (uiCallbacks) {
			int size = uiCallbacks.size();
			if (size > 0) {
				uiCallbacks.get(size - 1).onBluetoothActionHappened(event);
			}
		}
	}

	public boolean isBluetoothSupported() {
		return bluetoothSupported;
	}

	/**
	 * 当{@link cn.bit.hao.ble.tool.service.MonitorConnectivityService}监听到状态变化时，调用此方法修改状态。
	 * 而本Manager则会将此事件通知到各个注册着的回调函数
	 *
	 * @param newState
	 */
	public void setBluetoothState(int newState) {
		if (bluetoothState == newState) {
			return;
		}
		if (bluetoothState == BluetoothAdapter.ERROR) {
			sendActionCode(new BluetoothEvent(BluetoothEvent.BluetoothCode.STATE_ERROR_CODE));
			return;
		}
		bluetoothState = newState;
		BluetoothEvent event = new BluetoothEvent(BluetoothEvent.BluetoothCode.STATE_CHANGED_CODE);
		Bundle eventData = new Bundle();
		eventData.putInt(BluetoothEvent.NEW_BLUETOOTH_STATE, bluetoothState);
		event.setEventData(eventData);
		sendActionCode(event);
	}

	/**
	 * <p>返回蓝牙的状态，可能是{@link BluetoothAdapter#getState()}的返回值</p>
	 *
	 * @return 如果返回{@link BluetoothAdapter#ERROR}，表示蓝牙功能异常，否则返回蓝牙状态
	 */
	public int getBluetoothState() {
		return bluetoothState;
	}

}
