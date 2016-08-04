/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.events;

/**
 * @author wuhao on 2016/7/30
 */
public class BluetoothStateEvent extends CommonResponseEvent {
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

	public BluetoothStateEvent(BluetoothStateEvent bluetoothStateEvent) {
		this.eventCode = bluetoothStateEvent.eventCode;
	}

	public BluetoothStateCode getEventCode() {
		return eventCode;
	}

	public void setEventCode(BluetoothStateCode eventCode) {
		this.eventCode = eventCode;
	}

	@Override
	public BluetoothStateEvent clone() {
		BluetoothStateEvent result = null;
		try {
			result = (BluetoothStateEvent) super.clone();
			result.eventCode = this.eventCode;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("eventCode: ").append(eventCode == null ? "null" : eventCode);
		return sb.toString();
	}
}
