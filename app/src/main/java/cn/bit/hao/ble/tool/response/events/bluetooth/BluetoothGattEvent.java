/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.events.bluetooth;

import cn.bit.hao.ble.tool.response.events.CommonEvent;

/**
 * @author wuhao on 2016/8/1
 */
public class BluetoothGattEvent extends CommonEvent {
	private static final String TAG = BluetoothGattEvent.class.getSimpleName();

	public enum BluetoothGattCode {
		GATT_SCAN_DEVICE_TIMEOUT,
		/**
		 * 默认当读取完gatt services的列表后才算连接建立
		 */
		GATT_CONNECTED,
		GATT_DISCONNECTED,
		/**
		 * 通常来说，对于应用层而言，GATT_CLOSED是较为可靠的连接断开标志，
		 * 而GATT_DISCONNECTED只是内部逻辑的连接断开标志
		 */
		GATT_CLOSED,
		GATT_CONNECT_TIMEOUT,
		GATT_REMOTE_DISAPPEARED,
		GATT_CONNECTION_ERROR
	}

	private String macAddress;
	private BluetoothGattCode eventCode;

	public BluetoothGattEvent(String macAddress, BluetoothGattCode eventCode) {
		this.macAddress = macAddress;
		this.eventCode = eventCode;
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
		sb.append("BluetoothGattEvent  ").append("macAddress: ").append(macAddress == null ? "null" : macAddress)
				.append(", ").append("eventCode: ").append(eventCode == null ? "null" : eventCode);
		return sb.toString();
	}
}
