/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.events;

/**
 * @author wuhao on 2016/7/30
 */
public class BluetoothStateEvent extends ResponseEvent {
	private static final String TAG = BluetoothStateEvent.class.getSimpleName();

	public enum BluetoothStateCode {
		BLUETOOTH_STATE_ON,
		BLUETOOTH_STATE_OFF,
		BLUETOOTH_STATE_ERROR
	}

	public BluetoothStateCode eventCode;

	public BluetoothStateEvent(BluetoothStateCode eventCode) {
		super();
		this.eventCode = eventCode;
	}

	public BluetoothStateEvent(BluetoothStateEvent bluetoothStateEvent) {
		super(bluetoothStateEvent);
		this.eventCode = bluetoothStateEvent.eventCode;
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
}
