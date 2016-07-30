/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.events;

import android.os.Bundle;

/**
 * @author wuhao on 2016/7/30
 */
public class BluetoothEvent {
	private static final String TAG = BluetoothEvent.class.getSimpleName();

	public enum BluetoothCode {
		BLUETOOTH_NOT_SUPPORTED_CODE,
		/**
		 * 附带有新状态值{@link #NEW_BLUETOOTH_STATE}
		 */
		STATE_CHANGED_CODE,
		STATE_ERROR_CODE
	}

	/**
	 * 蓝牙状态值，可能是{@link android.bluetooth.BluetoothAdapter#STATE_ON}等
	 */
	public static final String NEW_BLUETOOTH_STATE = BluetoothEvent.class.getCanonicalName() + ".NEW_BLUETOOTH_STATE";

	public BluetoothCode eventCode;
	private Bundle eventData;

	public BluetoothEvent(BluetoothCode eventCode) {
		this.eventCode = eventCode;
	}

	public BluetoothEvent(BluetoothEvent event) {
		this.eventCode = event.eventCode;
		this.eventData = event.eventData != null ? new Bundle(event.eventData) : null;
	}

	public Bundle getEventData() {
		return eventData;
	}

	public void setEventData(Bundle eventData) {
		this.eventData = eventData;
	}
}
