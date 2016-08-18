/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.events.bluetooth;

import cn.bit.hao.ble.tool.bluetooth.utils.ScanRecordCompat;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;
import cn.bit.hao.ble.tool.util.ByteBitUtil;

/**
 * @author wuhao on 2016/8/4
 */
public class BluetoothLeScanResultEvent extends CommonResponseEvent {
	private static final String TAG = BluetoothLeScanResultEvent.class.getSimpleName();

	private String macAddress;
	private int rssi;
	private ScanRecordCompat scanRecord;

	public BluetoothLeScanResultEvent(String macAddress, int rssi, ScanRecordCompat scanRecord) {
		this.macAddress = macAddress;
		this.rssi = rssi;
		this.scanRecord = scanRecord;
	}

	public ScanRecordCompat getScanRecord() {
		return scanRecord;
	}

	public void setScanRecord(ScanRecordCompat scanRecord) {
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
	public BluetoothLeScanResultEvent clone() {
		BluetoothLeScanResultEvent result = null;
		try {
			result = (BluetoothLeScanResultEvent) super.clone();
			result.scanRecord = this.scanRecord == null ? null : this.scanRecord.deepClone();
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
				.append("scanRecord: ").append(ByteBitUtil.byteArrayToHexString(scanRecord.getBytes()))
				.append("\n").append(scanRecord);
		return sb.toString();
	}
}
