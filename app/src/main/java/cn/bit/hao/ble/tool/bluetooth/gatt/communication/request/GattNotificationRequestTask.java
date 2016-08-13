/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt.communication.request;

import java.util.UUID;

/**
 * @author wuhao on 2016/8/13
 */
public class GattNotificationRequestTask extends GattRequestTask {
	private static final String TAG = GattNotificationRequestTask.class.getSimpleName();

	private boolean enable = true;

	public GattNotificationRequestTask(String macAddress, UUID service, UUID characteristic, boolean enable) {
		super(macAddress, service, characteristic);
		this.enable = enable;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	@Override
	public boolean equals(Object o) {
		boolean result = super.equals(o);
		if (!result) {
			return false;
		}
		if (!(o instanceof GattNotificationRequestTask)) {
			return false;
		}
		GattNotificationRequestTask task = (GattNotificationRequestTask) o;
		return this.enable == task.enable;
	}
}
