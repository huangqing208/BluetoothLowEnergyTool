/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.scan;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.util.Log;

import java.util.List;

import cn.bit.hao.ble.tool.bluetooth.utils.ScanRecordCompat;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothLeScanEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothLeScanResultEvent;
import cn.bit.hao.ble.tool.response.manager.CommonEventManager;

/**
 * @author wuhao on 2016/8/4
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScanCallbackImpl extends ScanCallback {
	private static final String TAG = ScanCallbackImpl.class.getSimpleName();

	@Override
	public void onScanResult(int callbackType, ScanResult result) {
		super.onScanResult(callbackType, result);
		ScanRecord scanRecord = result.getScanRecord();
		if (scanRecord == null) {
			return;
		}
		CommonEventManager.getInstance().sendResponse(
				new BluetoothLeScanResultEvent(result.getDevice(), result.getRssi(),
						new ScanRecordCompat(scanRecord)));
	}

	@Override
	public void onBatchScanResults(List<ScanResult> results) {
		super.onBatchScanResults(results);
		Log.i(TAG, "onBatchScanResults " + (results != null ? results.size() : 0));
	}

	@Override
	public void onScanFailed(int errorCode) {
		super.onScanFailed(errorCode);
		Log.e(TAG, "onScanFailed " + errorCode);
		if (errorCode == SCAN_FAILED_ALREADY_STARTED) {
			return;
		}
		CommonEventManager.getInstance().sendResponse(
				new BluetoothLeScanEvent(BluetoothLeScanEvent.BluetoothLeScanCode.LE_SCAN_FAILED));
	}
}
