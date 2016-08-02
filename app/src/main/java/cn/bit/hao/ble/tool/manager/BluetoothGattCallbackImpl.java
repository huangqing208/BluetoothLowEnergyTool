/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.manager;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Bundle;
import android.util.Log;

import cn.bit.hao.ble.tool.events.BluetoothGattEvent;
import cn.bit.hao.ble.tool.events.ResponseEvent;

/**
 * @author wuhao on 2016/8/1
 */
public class BluetoothGattCallbackImpl extends BluetoothGattCallback {
	private static final String TAG = BluetoothGattCallbackImpl.class.getSimpleName();

	@Override
	public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
		BluetoothGattEvent bluetoothGattEvent;
		if (status != BluetoothGatt.GATT_SUCCESS) {
			// 通常这是因为连接超时
			BluetoothGattManager.getInstance().closeGatt(gatt.getDevice().getAddress(), true);
			Log.w(TAG, gatt.getDevice().getAddress() + " onConnectionStateChange status: " + status + " newState: " + newState);
			bluetoothGattEvent = new BluetoothGattEvent(BluetoothGattEvent.BluetoothGattCode.GATT_CONNECT_TIMEOUT);
//			return;
		} else {
			switch (newState) {
				case BluetoothGatt.STATE_CONNECTED: {
					Log.i(TAG, gatt.getDevice().getAddress() + " onConnectionStateChange STATE_CONNECTED");
					bluetoothGattEvent = new BluetoothGattEvent(BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTED);
					break;
				}
				case BluetoothGatt.STATE_DISCONNECTED: {
					Log.i(TAG, gatt.getDevice().getAddress() + " onConnectionStateChange STATE_DISCONNECTED");
					bluetoothGattEvent = new BluetoothGattEvent(BluetoothGattEvent.BluetoothGattCode.GATT_DISCONNECTED);
					break;
				}
				default:
					Log.i(TAG, gatt.getDevice().getAddress() + " onConnectionStateChange ooh no");
					bluetoothGattEvent = null;
					super.onConnectionStateChange(gatt, status, newState);
					break;
			}
		}
		if (bluetoothGattEvent != null) {
			Bundle eventData = new Bundle();
			eventData.putString(ResponseEvent.EXTRA_DEVICE_MAC_ADDRESS, gatt.getDevice().getAddress());
			bluetoothGattEvent.setEventData(eventData);
			CommonResponseManager.getInstance().sendResponse(bluetoothGattEvent);
		}
	}

	@Override
	public void onServicesDiscovered(BluetoothGatt gatt, int status) {
		super.onServicesDiscovered(gatt, status);
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
	}

	@Override
	public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
		super.onCharacteristicRead(gatt, characteristic, status);
	}
}
