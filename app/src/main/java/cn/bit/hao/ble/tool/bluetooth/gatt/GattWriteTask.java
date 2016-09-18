/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.Arrays;
import java.util.UUID;

import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothGattEvent;
import cn.bit.hao.ble.tool.response.manager.CommonEventManager;

/**
 * @author wuhao on 2016/8/13
 */
public class GattWriteTask extends GattRequestTask {
	private static final String TAG = GattWriteTask.class.getSimpleName();

	private int writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
	private byte[] content;

	public GattWriteTask(String macAddress, UUID serviceUuid, UUID characteristicUuid,
	                     int writeType, byte[] content) {
		super(macAddress, serviceUuid, characteristicUuid);
		this.writeType = writeType;
		this.content = content;
	}

	public int getWriteType() {
		return writeType;
	}

	public void setWriteType(int writeType) {
		this.writeType = writeType;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
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
		characteristic.setValue(content);
		characteristic.setWriteType(writeType);
		boolean result = bluetoothGatt.writeCharacteristic(characteristic);
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
		if (!(o instanceof GattWriteTask)) {
			return false;
		}
		GattWriteTask task = (GattWriteTask) o;
		return this.writeType == task.writeType && Arrays.equals(this.content, task.content);
	}
}
