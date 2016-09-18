/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothGattEvent;
import cn.bit.hao.ble.tool.response.manager.CommonEventManager;

/**
 *
 *
 * @author wuhao on 2016/8/13
 */
public class GattReadTask extends GattRequestTask {
	private static final String TAG = GattReadTask.class.getSimpleName();

	public GattReadTask(String macAddress, UUID serviceUuid, UUID characteristicUuid) {
		super(macAddress, serviceUuid, characteristicUuid);
	}

	@Override
	public boolean execute() {
		BluetoothGatt bluetoothGatt = BluetoothGattManager.getInstance().getBluetoothGatt(macAddress);
		if (bluetoothGatt == null) {
			return false;
		}
		BluetoothGattService service = bluetoothGatt.getService(serviceUuid);
		if (service == null) {
			return false;
		}
		BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
		if (characteristic == null) {
			return false;
		}
		boolean result = bluetoothGatt.readCharacteristic(characteristic);
		if (!result) {
			// 并不太清楚出错的原因是什么，可能是连接问题，也可能是设备忙。
			// 解决方案倾向于对严重情况的处理，即处理连接问题。
			CommonEventManager.getInstance().sendResponse(new BluetoothGattEvent(macAddress,
					BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTION_ERROR));
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		boolean result = super.equals(o);
		if (!result) {
			return false;
		}
		return o instanceof GattReadTask;
	}
}
