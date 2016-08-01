/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import cn.bit.hao.ble.tool.callbacks.CommonResponseCallback;
import cn.bit.hao.ble.tool.data.BLEDevice;
import cn.bit.hao.ble.tool.data.DeviceStore;
import cn.bit.hao.ble.tool.events.BluetoothStateEvent;
import cn.bit.hao.ble.tool.events.ResponseEvent;
import cn.bit.hao.ble.tool.utils.BluetoothUtil;

/**
 * 管理App同设备的连接
 *
 * @author wuhao on 2016/7/18
 */
public class BluetoothGattManager implements CommonResponseCallback {
	private static final String TAG = BluetoothGattManager.class.getSimpleName();

	private WeakReference<Context> communicationServiceContext;

	// 保存当前的各个Gatt连接对象
	private Map<String, BluetoothGatt> bluetoothGattMap;

	private static BluetoothGattManager instance;

	private BluetoothGattManager() {
		bluetoothGattMap = new HashMap<>();
	}

	public static synchronized BluetoothGattManager getInstance(Context context) {
		if (instance == null) {
			instance = new BluetoothGattManager();
			// 确保监听状态变化，可以在蓝牙不工作的时候做出反应
			CommonResponseManager.getInstance().addTaskCallback(instance);
			instance.communicationServiceContext = new WeakReference<Context>(context);
		}
		return instance;
	}

	/**
	 * 建立连接到目标设备的Gatt连接，并且开始管理此Gatt连接
	 *
	 * @param context    上下文对象
	 * @param macAddress 目标设备mac地址
	 * @return 如果连接成功则返回true，否则返回false
	 */
	public boolean connectDevice(Context context, String macAddress) {
		if (!BluetoothStateManager.getInstance(context).isBluetoothSupported()) {
			// 如果设备不支持蓝牙，那么从创建连接开始就拒绝动作即可
			return false;
		}
		if (BluetoothStateManager.getInstance(context).getBluetoothState() != BluetoothAdapter.STATE_ON) {
			// 如果蓝牙关闭的话，则暂停连接服务
			return false;
		}
		if (!BluetoothUtil.checkBluetoothAddress(macAddress)) {
			return false;
		}
		BLEDevice bleDevice = DeviceStore.getInstance().getDevice(macAddress);
		if (bleDevice == null) {
			return false;
		}
		synchronized (bluetoothGattMap) {
			if (bluetoothGattMap.get(macAddress) != null) {
				// 如果存在对应的Gatt对象，则表示连接已存在，
				return false;
			}

			BluetoothDevice bluetoothDevice = BluetoothUtil.getBluetoothDevice(context, macAddress);

			long time = SystemClock.uptimeMillis();
			BluetoothGatt bluetoothGatt = bluetoothDevice.connectGatt(context, false, new BluetoothGattCallbackImpl());
			Log.i(TAG, "connectGatt costs " + (SystemClock.uptimeMillis() - time) + " ms");

			bluetoothGattMap.put(macAddress, bluetoothGatt);
			return true;
		}
	}

	/**
	 * 获取Gatt连接的状态
	 *
	 * @param context    上下文对象
	 * @param macAddress 目标设备的mac地址
	 * @return 如果有和目标设备建立Gatt连接的话，则返回{@link BluetoothAdapter#STATE_CONNECTED}，否则返回{@link BluetoothAdapter#STATE_DISCONNECTED}
	 */
	public int getBluetoothGattState(Context context, String macAddress) {
		return BluetoothUtil.getBluetoothGattState(context, macAddress);
	}

	/**
	 * 断开和目标设备的Gatt连接
	 *
	 * @param macAddress 目标设备的mac地址
	 * @param removeGatt 如果设置为true，则会完全删除目标设备信息，如果设置为false，则保留目标设备的mac地址，后续可以尝试恢复连接
	 * @return 被断开的Gatt连接对象
	 */
	public BluetoothGatt closeGatt(String macAddress, boolean removeGatt) {
		BluetoothGatt bluetoothGatt;
		synchronized (bluetoothGattMap) {
			if (removeGatt) {
				bluetoothGatt = bluetoothGattMap.remove(macAddress);
			} else {
				bluetoothGatt = bluetoothGattMap.get(macAddress);
				if (bluetoothGatt != null) {
					// 仅仅是将value置null，并不remove此item
					bluetoothGattMap.put(macAddress, null);
				}
			}
		}
		if (bluetoothGatt != null) {
			long time = SystemClock.uptimeMillis();
			bluetoothGatt.close();
			Log.i(TAG, "close gatt costs " + (SystemClock.uptimeMillis() - time) + " ms");
		}
		return bluetoothGatt;
	}

	/**
	 * 删除Gatt连接
	 *
	 * @param macAddress 目标设备的mac地址
	 * @return 如果成功删除则返回true，否则返回false
	 */
	public boolean removeGatt(String macAddress) {
		return closeGatt(macAddress, true) != null;
	}

	/**
	 * 调用此方法可以结束所有工作
	 */
	public void removeAllGatts() {
		for (String macAddress : bluetoothGattMap.keySet()) {
			removeGatt(macAddress);
		}
	}

	@Override
	public void onCommonResponded(ResponseEvent responseEvent) {
		// 如果反馈的事件并非蓝牙状态相关的，则不关注
		if (responseEvent instanceof BluetoothStateEvent) {
			switch (((BluetoothStateEvent) responseEvent).eventCode) {
				case BLUETOOTH_STATE_ON:
					// 恢复存在mac地址而不存在gatt对象的gatt连接
					for (Map.Entry<String, BluetoothGatt> item : bluetoothGattMap.entrySet()) {
						if (item.getValue() == null && communicationServiceContext.get() != null) {
							connectDevice(communicationServiceContext.get(), item.getKey());
						}
					}
					break;
				case BLUETOOTH_STATE_OFF:
					// 断开所有蓝牙连接，恢复连接后则重连
					for (String macAddress : bluetoothGattMap.keySet()) {
						closeGatt(macAddress, false);
					}
					break;
				case BLUETOOTH_STATE_ERROR:
					// 忽略这种情况，因为情形未知
				default:
					break;
			}
		}
	}
}
