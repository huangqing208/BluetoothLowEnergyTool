/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;

import java.util.Map;

import cn.bit.hao.ble.tool.events.BluetoothEvent;
import cn.bit.hao.ble.tool.interfaces.BluetoothCallback;
import cn.bit.hao.ble.tool.utils.BluetoothUtil;

/**
 * 管理App同设备的连接
 *
 * @author wuhao on 2016/7/18
 */
public class BluetoothGattManager implements BluetoothCallback {
	private static final String TAG = BluetoothGattManager.class.getSimpleName();

	private static BluetoothGattManager instance;

	private BluetoothGattManager() {
	}

	public static synchronized BluetoothGattManager getInstance(Context context) {
		if (instance == null) {
			instance = new BluetoothGattManager();
			instance.initManager(context);
		}
		return instance;
	}

	private Map<String, BluetoothGatt> bluetoothGattMap;

	public boolean connectDevice(Context context, String mac) {
		if (!BluetoothStateManager.getInstance(context).isBluetoothSupported()) {
			return false;
		}
		BluetoothAdapter bluetoothAdapter = BluetoothUtil.getBluetoothAdapter(context);
		BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);

		return false;
	}

	private void initManager(Context context) {
		// 确保监听状态变化，可以在蓝牙不工作的时候做出反应
		BluetoothStateManager.getInstance(context).addTaskCallback(this);
	}

	@Override
	public void onBluetoothActionHappened(BluetoothEvent bluetoothEvent) {
		switch (bluetoothEvent.eventCode) {
			case STATE_CHANGED_CODE:
				if (bluetoothEvent.getEventData().getInt(BluetoothEvent.NEW_BLUETOOTH_STATE) == BluetoothAdapter.STATE_OFF) {

				}
				break;
			case STATE_ERROR_CODE:
				// 忽略这种情况，因为情形未知
			case BLUETOOTH_NOT_SUPPORTED_CODE:
				// 这里不会发生
			default:
				break;
		}
	}
}
