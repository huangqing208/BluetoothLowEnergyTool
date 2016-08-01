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
		if (status != BluetoothGatt.GATT_SUCCESS) {
			return;
		}
		Log.i(TAG, gatt.getDevice().getAddress() + " onConnectionStateChange newState: " + newState);
		switch (newState) {
			case BluetoothGatt.STATE_CONNECTED: {
				BluetoothGattEvent bluetoothGattEvent = new BluetoothGattEvent(BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTED);
				Bundle eventData = new Bundle();
				eventData.putString(ResponseEvent.EXTRA_DEVICE_MAC_ADDRESS, gatt.getDevice().getAddress());
				bluetoothGattEvent.setEventData(eventData);
				CommonResponseManager.getInstance().sendResponse(bluetoothGattEvent);
				break;
			}
			case BluetoothGatt.STATE_DISCONNECTED: {
				BluetoothGattEvent bluetoothGattEvent = new BluetoothGattEvent(BluetoothGattEvent.BluetoothGattCode.GATT_DISCONNECTED);
				Bundle eventData = new Bundle();
				eventData.putString(ResponseEvent.EXTRA_DEVICE_MAC_ADDRESS, gatt.getDevice().getAddress());
				bluetoothGattEvent.setEventData(eventData);
				CommonResponseManager.getInstance().sendResponse(bluetoothGattEvent);
				break;
			}
			default:
				super.onConnectionStateChange(gatt, status, newState);
				break;
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
