/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.bit.hao.ble.tool.bluetooth.gatt.GattResponseListener;
import cn.bit.hao.ble.tool.bluetooth.gatt.GattResponseManager;
import cn.bit.hao.ble.tool.data.device.bluetooth.BLEDevice;

/**
 * @author wuhao on 2016/7/14
 */
public class DeviceStore implements GattResponseListener {

	private Map<String, BLEDevice> bleDeviceMap;

	private DeviceStore() {
		bleDeviceMap = new HashMap<>();
	}

	private static DeviceStore instance;

	public static synchronized DeviceStore getInstance() {
		if (instance == null) {
			instance = new DeviceStore();
			GattResponseManager.getInstance().setGattResponseListener(instance);
		}
		return instance;
	}

	public synchronized boolean addDevice(BLEDevice BLEDevice) {
		if (bleDeviceMap.containsKey(BLEDevice.getMacAddress())) {
			return false;
		}
		bleDeviceMap.put(BLEDevice.getMacAddress(), BLEDevice);
		// 如果需要的话，在此做持久存储操作

		return true;
	}

	public BLEDevice getDevice(String macAddress) {
		return bleDeviceMap.get(macAddress);
	}

	public synchronized boolean removeDevice(String macAddress) {
		BLEDevice bleDevice = bleDeviceMap.remove(macAddress);
		if (bleDevice == null) {
			return false;
		}
		// 如果需要的话，在此作持久存储操作

		return true;
	}

	@Override
	public boolean parseGattResponse(String macAddress, UUID serviceUuid, UUID characteristicUuid,
	                                 byte[] content) {
		BLEDevice device = getDevice(macAddress);
		if (device == null) {
			return false;
		}
		device.parse(serviceUuid, characteristicUuid, content);
		return true;
	}
}
