/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt;

import java.util.UUID;

/**
 * @author wuhao on 2016/8/13
 */
public abstract class GattRequestTask {
	private static final String TAG = GattRequestTask.class.getSimpleName();
	protected String macAddress;
	protected UUID serviceUuid;
	protected UUID characteristicUuid;

	public GattRequestTask(String macAddress, UUID serviceUuid, UUID characteristicUuid) {
		this.macAddress = macAddress;
		this.serviceUuid = serviceUuid;
		this.characteristicUuid = characteristicUuid;
	}

	public UUID getCharacteristicUuid() {
		return characteristicUuid;
	}

	public void setCharacteristicUuid(UUID characteristicUuid) {
		this.characteristicUuid = characteristicUuid;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public UUID getServiceUuid() {
		return serviceUuid;
	}

	public void setServiceUuid(UUID serviceUuid) {
		this.serviceUuid = serviceUuid;
	}

	/**
	 * 正式执行任务
	 *
	 * @return 成功执行的话返回true，否则返回false
	 */
	public abstract boolean execute();

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
				&& this.serviceUuid.equals(task.serviceUuid)
				&& this.characteristicUuid.equals(task.characteristicUuid);
	}

}
