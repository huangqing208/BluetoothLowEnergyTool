/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.events.bluetooth;

import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;

/**
 * @author wuhao on 2016/8/1
 */
public class BluetoothGattEvent extends CommonResponseEvent {
	private static final String TAG = BluetoothGattEvent.class.getSimpleName();

	public enum BluetoothGattCode {
		GATT_CONNECTED,
		GATT_DISCONNECTED,
		GATT_CONNECT_TIMEOUT,
		GATT_REMOTE_DISAPPEARED,
		GATT_CONNECTION_ERROR,
		GATT_SERVICES_DISCOVERED
	}

	private BluetoothGattCode eventCode;
	private String macAddress;

	public BluetoothGattEvent(BluetoothGattCode eventCode, String macAddress) {
		this.eventCode = eventCode;
		this.macAddress = macAddress;
	}

	public BluetoothGattEvent(BluetoothGattEvent bluetoothGattEvent) {
		this.eventCode = bluetoothGattEvent.eventCode;
		this.macAddress = bluetoothGattEvent.macAddress;
	}

	public BluetoothGattCode getEventCode() {
		return eventCode;
	}

	public void setEventCode(BluetoothGattCode eventCode) {
		this.eventCode = eventCode;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("eventCode: ").append(eventCode == null ? "null" : eventCode).append(", ")
				.append("macAddress: ").append(macAddress == null ? "null" : macAddress);
		return sb.toString();
	}
}
