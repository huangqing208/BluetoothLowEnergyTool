/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;

/**
 * @author wuhao on 2016/7/30
 */
public class BluetoothUtil {
	private static final String TAG = BluetoothUtil.class.getSimpleName();

	public static BluetoothAdapter getBluetoothAdapter(Context context) {
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return BluetoothAdapter.getDefaultAdapter();
		} else {
			BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
			return bluetoothManager.getAdapter();
		}
	}
}
