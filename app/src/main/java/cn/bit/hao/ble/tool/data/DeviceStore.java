/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wuhao on 2016/7/14
 */
public class DeviceStore {

	private List<BLEDevice> BLEDeviceList;

	private DeviceStore() {
		BLEDeviceList = new ArrayList<>();
	}

	private static DeviceStore instance = new DeviceStore();

	public static DeviceStore getInstance() {
		return instance;
	}

	public BLEDevice getDevice(int index) {
		if (index < 0 || index >= BLEDeviceList.size()) {
			return null;
		}
		return BLEDeviceList.get(index);
	}

	public int addDevice(BLEDevice BLEDevice) {
		if (BLEDeviceList.contains(BLEDevice)) {
			return -1;
		}
		BLEDeviceList.add(BLEDevice);
		// 如果需要的话，在此做持久存储操作

		return BLEDeviceList.size() - 1;
	}

	public void removeDevice(BLEDevice BLEDevice) {
		BLEDeviceList.remove(BLEDevice);
	}

}
