/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt.communication;

import android.bluetooth.BluetoothGattCharacteristic;

import cn.bit.hao.ble.tool.data.DeviceStore;

/**
 * 设备返回的接收处理
 * TODO: 此类中处理业务相关的逻辑，所以是需要定制的
 *
 * @author wuhao on 2016/8/11
 */
public class GattResponseManager {
	private static final String TAG = GattResponseManager.class.getSimpleName();

	private static GattResponseManager instance;

	private GattResponseManager() {
	}

	public static synchronized GattResponseManager getInstance() {
		if (instance == null) {
			instance = new GattResponseManager();
		}
		return instance;
	}

	public void receiveResponse(String macAddress, BluetoothGattCharacteristic characteristic) {
		// 在此判断返回的characteristic是否是预期的那个，如果是的话，才允许后续
		DeviceStore.getInstance().parseResponse(macAddress, characteristic.getValue());
	}

}
