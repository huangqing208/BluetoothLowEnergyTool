/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.events;

/**
 * @author wuhao on 2016/8/1
 */
public class BluetoothGattEvent extends ResponseEvent {
	private static final String TAG = BluetoothGattEvent.class.getSimpleName();

	public enum BluetoothGattCode {
		GATT_CONNECTED,
		GATT_DISCONNECTED,
		GATT_CONNECT_TIMEOUT
	}

	public BluetoothGattCode eventCode;

	public BluetoothGattEvent(BluetoothGattCode eventCode) {
		super();
		this.eventCode = eventCode;
	}

	public BluetoothGattEvent(BluetoothGattEvent bluetoothGattEvent) {
		super(bluetoothGattEvent);
		this.eventCode = bluetoothGattEvent.eventCode;
	}

	@Override
	public BluetoothGattEvent clone() {
		BluetoothGattEvent result = null;
		try {
			result = (BluetoothGattEvent) super.clone();
			result.eventCode = this.eventCode;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return result;
	}
}
