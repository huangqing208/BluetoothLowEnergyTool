/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.events.bluetooth;

import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;

/**
 * @author wuhao on 2016/8/15
 */
public class BluetoothGattResponseEvent extends CommonResponseEvent {
	private static final String TAG = BluetoothGattResponseEvent.class.getSimpleName();

	public enum BluetoothGattResponseCode {
		CHARACTERISTIC_READ,
		CHARACTERISTIC_WRITE,
		CHARACTERISTIC_CHANGED,
		DESCRIPTOR_READ,
		DESCRIPTOR_WRITE
	}

	private String macAddress;
	private String serviceUuid;
	private String characteristicUuid;
	private String descriptorUuid;
	private BluetoothGattResponseCode bluetoothGattResponseCode;

	public BluetoothGattResponseEvent(String macAddress, String serviceUuid, String characteristicUuid,
	                                  BluetoothGattResponseCode bluetoothGattResponseCode) {
		this(macAddress, serviceUuid, characteristicUuid, null, bluetoothGattResponseCode);
	}

	public BluetoothGattResponseEvent(String macAddress, String serviceUuid,
	                                  String characteristicUuid, String descriptorUuid,
	                                  BluetoothGattResponseCode bluetoothGattResponseCode) {
		this.macAddress = macAddress;
		this.serviceUuid = serviceUuid;
		this.characteristicUuid = characteristicUuid;
		this.descriptorUuid = descriptorUuid;
		this.bluetoothGattResponseCode = bluetoothGattResponseCode;
	}

	public BluetoothGattResponseCode getBluetoothGattResponseCode() {
		return bluetoothGattResponseCode;
	}

	public void setBluetoothGattResponseCode(BluetoothGattResponseCode bluetoothGattResponseCode) {
		this.bluetoothGattResponseCode = bluetoothGattResponseCode;
	}

	public String getCharacteristicUuid() {
		return characteristicUuid;
	}

	public void setCharacteristicUuid(String characteristicUuid) {
		this.characteristicUuid = characteristicUuid;
	}

	public String getDescriptorUuid() {
		return descriptorUuid;
	}

	public void setDescriptorUuid(String descriptorUuid) {
		this.descriptorUuid = descriptorUuid;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getServiceUuid() {
		return serviceUuid;
	}

	public void setServiceUuid(String serviceUuid) {
		this.serviceUuid = serviceUuid;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("eventCode: ").append(bluetoothGattResponseCode == null ?	"null" : bluetoothGattResponseCode).append(", ")
				.append("macAddress: ").append(macAddress == null ? "null" : macAddress).append(", ")
				.append("serviceUuid: ").append(serviceUuid == null ? "null" : serviceUuid).append(", ")
				.append("characteristicUuid: ").append(characteristicUuid == null ? "null" : characteristicUuid).append(", ")
				.append("descriptorUuid: ").append(descriptorUuid == null ? "null" : descriptorUuid);
		return sb.toString();
	}
}
