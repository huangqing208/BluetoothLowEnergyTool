/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.manager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;

import cn.bit.hao.ble.tool.events.BluetoothStateEvent;
import cn.bit.hao.ble.tool.utils.BluetoothUtil;

/**
 * 此Manager的最大作用就是管理蓝牙状态，并且向需要接收状态变化的对象通知状态变化事件
 *
 * @author wuhao on 2016/7/16
 */
public class BluetoothStateManager {
	private static final String TAG = BluetoothStateManager.class.getSimpleName();

	private static BluetoothStateManager instance;

	/**
	 * 默认设备支持蓝牙
	 */
	private boolean bluetoothSupported = true;

	private int bluetoothState = BluetoothAdapter.ERROR;

	private BluetoothStateManager() {
	}

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
			CommonResponseManager.getInstance().sendResponse(new BluetoothStateEvent(
					BluetoothStateEvent.BluetoothStateCode.BLUETOOTH_STATE_ERROR));
			return;
		}
		bluetoothState = newState;
		BluetoothStateEvent event;
		switch (bluetoothState) {
			case BluetoothAdapter.STATE_ON:
				event = new BluetoothStateEvent(BluetoothStateEvent.BluetoothStateCode.BLUETOOTH_STATE_ON);
				break;
			case BluetoothAdapter.STATE_OFF:
				event = new BluetoothStateEvent(BluetoothStateEvent.BluetoothStateCode.BLUETOOTH_STATE_OFF);
				break;
			default:
				event = null;
				break;
		}
		if (event != null) {
			CommonResponseManager.getInstance().sendResponse(event);
		}
	}

	/**
	 * <p>返回蓝牙的状态，可能是{@link BluetoothAdapter#getState()}的返回值</p>
	 *
	 * @return 如果返回{@link BluetoothAdapter#ERROR}，表示设备不支持蓝牙功能，否则返回蓝牙状态
	 */
	public int getBluetoothState() {
		return bluetoothState;
	}

}
