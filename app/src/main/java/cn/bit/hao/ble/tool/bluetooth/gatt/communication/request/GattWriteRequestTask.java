/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt.communication.request;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.Arrays;
import java.util.UUID;

/**
 * @author wuhao on 2016/8/13
 */
public class GattWriteRequestTask extends GattRequestTask {
	private static final String TAG = GattWriteRequestTask.class.getSimpleName();

	private int writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT;
	private byte[] content;

	public GattWriteRequestTask(String macAddress, UUID service, UUID characteristic,
	                            int writeType, byte[] content) {
		super(macAddress, service, characteristic);
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
	public boolean equals(Object o) {
		boolean result = super.equals(o);
		if (!result) {
			return false;
		}
		if (!(o instanceof GattWriteRequestTask)) {
			return false;
		}
		GattWriteRequestTask task = (GattWriteRequestTask) o;
		return this.writeType == task.writeType && Arrays.equals(this.content, task.content);
	}
}
