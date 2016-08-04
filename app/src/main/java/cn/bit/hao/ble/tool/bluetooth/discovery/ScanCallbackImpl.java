/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.discovery;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;

import java.util.List;

/**
 * @author wuhao on 2016/8/4
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScanCallbackImpl extends ScanCallback {
	private static final String TAG = ScanCallbackImpl.class.getSimpleName();

	@Override
	public void onScanResult(int callbackType, ScanResult result) {
		super.onScanResult(callbackType, result);
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
