/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.scan;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;

import java.util.List;

import cn.bit.hao.ble.tool.response.events.BluetoothLeScanEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * @author wuhao on 2016/8/4
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScanCallbackImpl extends ScanCallback {
	private static final String TAG = ScanCallbackImpl.class.getSimpleName();

	@Override
	public void onScanResult(int callbackType, ScanResult result) {
		super.onScanResult(callbackType, result);
		CommonResponseManager.getInstance().sendResponse(
				new BluetoothLeScanEvent(result.getDevice().getAddress(), result.getRssi(),
						result.getScanRecord().getBytes()));
	}

	@Override
	public void onBatchScanResults(List<ScanResult> results) {
		super.onBatchScanResults(results);
	}

	@Override
	public void onScanFailed(int errorCode) {
		super.onScanFailed(errorCode);
	}
}
