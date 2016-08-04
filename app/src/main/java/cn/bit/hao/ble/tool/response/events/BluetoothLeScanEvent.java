/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.events;

/**
 * @author wuhao on 2016/8/4
 */
public class BluetoothLeScanEvent extends CommonResponseEvent {
	private static final String TAG = BluetoothLeScanEvent.class.getSimpleName();

	private String macAddress;

	public BluetoothLeScanEvent() {
	}

	@Override
	public BluetoothLeScanEvent clone() {
		BluetoothLeScanEvent result = null;
		try {
			result = (BluetoothLeScanEvent) super.clone();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("; ").append(super.toString());
		return sb.toString();
	}
}
