/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.events;

import java.util.Arrays;

import cn.bit.hao.ble.tool.bluetooth.utils.ByteBitUtil;

/**
 * @author wuhao on 2016/8/4
 */
public class BluetoothLeScanEvent extends CommonResponseEvent {
	private static final String TAG = BluetoothLeScanEvent.class.getSimpleName();

	private String macAddress;
	private int rssi;
	private byte[] scanRecord;

	public BluetoothLeScanEvent(String macAddress, int rssi, byte[] scanRecord) {
		this.macAddress = macAddress;
		this.rssi = rssi;
		this.scanRecord = scanRecord;
	}

	public byte[] getScanRecord() {
		return scanRecord;
	}

	public void setScanRecord(byte[] scanRecord) {
		this.scanRecord = scanRecord;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	@Override
	public BluetoothLeScanEvent clone() {
		BluetoothLeScanEvent result = null;
		try {
			result = (BluetoothLeScanEvent) super.clone();
			result.macAddress = this.macAddress;
			result.rssi = this.rssi;
			result.scanRecord = Arrays.copyOf(this.scanRecord, this.scanRecord.length);
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("macAddress: ").append(macAddress).append("; ")
				.append("rssi: ").append(rssi).append("; ")
				.append("scanRecord: ").append(ByteBitUtil.byteArrayToHexString(scanRecord));
		return sb.toString();
	}
}
