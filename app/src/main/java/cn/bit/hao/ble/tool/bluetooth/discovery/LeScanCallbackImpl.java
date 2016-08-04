/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.discovery;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * @author wuhao on 2016/8/4
 */
public class LeScanCallbackImpl implements BluetoothAdapter.LeScanCallback {
	private static final String TAG = LeScanCallbackImpl.class.getSimpleName();

	@Override
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

	}
}
