/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt.communication.request;

import java.util.UUID;

/**
 *
 *
 * @author wuhao on 2016/8/13
 */
public class GattReadRequestTask extends GattRequestTask {
	private static final String TAG = GattReadRequestTask.class.getSimpleName();

	public GattReadRequestTask(String macAddress, UUID service, UUID characteristic) {
		super(macAddress, service, characteristic);
	}

	@Override
	public boolean equals(Object o) {
		boolean result = super.equals(o);
		if (!result) {
			return false;
		}
		return o instanceof GattReadRequestTask;
	}
}
