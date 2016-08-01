/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;

/**
 * @author wuhao on 2016/7/30
 */
public class BluetoothUtil {
	private static final String TAG = BluetoothUtil.class.getSimpleName();

	/**
	 * 文档见{@link BluetoothAdapter#checkBluetoothAddress(String)}
	 */
	public static boolean checkBluetoothAddress(String macAddress) {
		return BluetoothAdapter.checkBluetoothAddress(macAddress);
	}

	/**
	 * 获取BluetoothManager对象
	 *
	 * @param context 上下文对象
	 * @return BluetoothManager对象
	 */
	private static BluetoothManager getBluetoothManager(Context context) {
		return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
	}

	/**
	 * 获取BluetoothAdapter对象
	 *
	 * @param context 上下文对象
	 * @return BluetoothAdapter对象
	 */
	public static BluetoothAdapter getBluetoothAdapter(Context context) {
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return BluetoothAdapter.getDefaultAdapter();
		} else {
			return getBluetoothManager(context).getAdapter();
		}
	}

	/**
	 * 获取目标设备的BluetoothDevice对象
	 *
	 * @param context    上下文对象
	 * @param macAddress 目标设备的mac地址
	 * @return 目标设备的BluetoothDevice对象
	 */
	public static BluetoothDevice getBluetoothDevice(Context context, String macAddress) {
		return getBluetoothAdapter(context).getRemoteDevice(macAddress);
	}

	/**
	 * 获取Gatt连接的状态
	 *
	 * @param context    上下文对象
	 * @param macAddress 目标设备的mac地址
	 * @return 如果有和目标设备建立Gatt连接的话，则返回{@link BluetoothAdapter#STATE_CONNECTED}，否则返回{@link BluetoothAdapter#STATE_DISCONNECTED}
	 */
	public static int getBluetoothGattState(Context context, String macAddress) {
		return getBluetoothManager(context).getConnectionState(
				getBluetoothDevice(context, macAddress), BluetoothProfile.GATT);
	}
}
