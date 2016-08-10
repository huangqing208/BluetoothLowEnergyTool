/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.util.Log;

import cn.bit.hao.ble.tool.data.DeviceStore;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothGattEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * @author wuhao on 2016/8/1
 */
public class BluetoothGattCallbackImpl extends BluetoothGattCallback {
	private static final String TAG = BluetoothGattCallbackImpl.class.getSimpleName();

	private static final int GATT_CONNECT_TIMEOUT = 133;
	private static final int REMOTE_DISAPPEARED = 8;

	@Override
	public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
		BluetoothGattEvent bluetoothGattEvent = null;
		String macAddress = gatt.getDevice().getAddress();
		Log.w(TAG, macAddress + " onConnectionStateChange status: " + status + " newState: " + newState);
		switch (status) {
			case BluetoothGatt.GATT_SUCCESS:
				switch (newState) {
					case BluetoothGatt.STATE_CONNECTED: {
						bluetoothGattEvent = new BluetoothGattEvent(BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTED, macAddress);
						break;
					}
					case BluetoothGatt.STATE_DISCONNECTED: {
						// 不一定是主动断开，可能是被动断开，但却是正常的被动断开
						bluetoothGattEvent = new BluetoothGattEvent(BluetoothGattEvent.BluetoothGattCode.GATT_DISCONNECTED, macAddress);
						break;
					}
					default:
						// TODO: 这里要不要处理呢？
						super.onConnectionStateChange(gatt, status, newState);
						break;
				}
				break;
			case GATT_CONNECT_TIMEOUT:
				// 通常这是因为连接超时
				bluetoothGattEvent = new BluetoothGattEvent(BluetoothGattEvent.BluetoothGattCode.GATT_CONNECT_TIMEOUT, macAddress);
				break;
			case REMOTE_DISAPPEARED:
				bluetoothGattEvent = new BluetoothGattEvent(BluetoothGattEvent.BluetoothGattCode.GATT_REMOTE_DISAPPEARED, macAddress);
				break;
			case BluetoothGatt.GATT_FAILURE:
			default:
				bluetoothGattEvent = new BluetoothGattEvent(BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTION_ERROR, macAddress);
				break;
		}
		if (bluetoothGattEvent != null) {
			CommonResponseManager.getInstance().sendResponse(bluetoothGattEvent);
		}
	}

	@Override
	public void onServicesDiscovered(BluetoothGatt gatt, int status) {
		super.onServicesDiscovered(gatt, status);
		if (status != BluetoothGatt.GATT_SUCCESS) {
			return;
		}
		CommonResponseManager.getInstance().sendResponse(
				new BluetoothGattEvent(BluetoothGattEvent.BluetoothGattCode.GATT_SERVICES_DISCOVERED,
						gatt.getDevice().getAddress()));
	}

	@Override
	public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
		super.onDescriptorWrite(gatt, descriptor, status);
	}

	@Override
	public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
		super.onCharacteristicWrite(gatt, characteristic, status);
	}

	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
		super.onCharacteristicChanged(gatt, characteristic);
		DeviceStore.getInstance().parseResponse(gatt.getDevice().getAddress(), characteristic.getValue());
	}

	@Override
	public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
		super.onCharacteristicRead(gatt, characteristic, status);
	}
}
