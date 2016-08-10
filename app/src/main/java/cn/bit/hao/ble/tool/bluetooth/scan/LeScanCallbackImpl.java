/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import cn.bit.hao.ble.tool.bluetooth.utils.ScanRecordCompat;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothLeScanResultEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * @author wuhao on 2016/8/4
 */
public class LeScanCallbackImpl implements BluetoothAdapter.LeScanCallback {
	private static final String TAG = LeScanCallbackImpl.class.getSimpleName();

	@Override
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
		CommonResponseManager.getInstance().sendResponse(
				new BluetoothLeScanResultEvent(device.getAddress(), rssi, ScanRecordCompat.parseFromBytes(scanRecord)));
	}
}
