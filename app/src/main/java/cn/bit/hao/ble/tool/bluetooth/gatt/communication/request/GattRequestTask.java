/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt.communication.request;

import java.util.UUID;

/**
 * @author wuhao on 2016/8/13
 */
public abstract class GattRequestTask {
	private static final String TAG = GattRequestTask.class.getSimpleName();
	private String macAddress;
	private UUID service;
	private UUID characteristic;

	public GattRequestTask(String macAddress, UUID service, UUID characteristic) {
		this.macAddress = macAddress;
		this.service = service;
		this.characteristic = characteristic;
	}

	public UUID getCharacteristic() {
		return characteristic;
	}

	public void setCharacteristic(UUID characteristic) {
		this.characteristic = characteristic;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public UUID getService() {
		return service;
	}

	public void setService(UUID service) {
		this.service = service;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof GattRequestTask)) {
			return false;
		}
		GattRequestTask task = (GattRequestTask) o;
		return this.macAddress.equals(task.macAddress)
				&& this.service.equals(task.service)
				&& this.characteristic.equals(task.characteristic);
	}
}
