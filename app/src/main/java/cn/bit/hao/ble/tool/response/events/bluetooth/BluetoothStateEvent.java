/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.events.bluetooth;

import cn.bit.hao.ble.tool.response.events.CommonEvent;

/**
 * @author wuhao on 2016/7/30
 */
public class BluetoothStateEvent extends CommonEvent {
	private static final String TAG = BluetoothStateEvent.class.getSimpleName();

	public enum BluetoothStateCode {
		BLUETOOTH_STATE_ON,
		BLUETOOTH_STATE_OFF,
		BLUETOOTH_STATE_ERROR
	}

	private BluetoothStateCode eventCode;

	public BluetoothStateEvent(BluetoothStateCode eventCode) {
		this.eventCode = eventCode;
	}

	public BluetoothStateCode getEventCode() {
		return eventCode;
	}

	public void setEventCode(BluetoothStateCode eventCode) {
		this.eventCode = eventCode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("eventCode: ").append(eventCode == null ? "null" : eventCode);
		return sb.toString();
	}
}
