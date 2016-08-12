/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.application;

import android.os.ParcelUuid;

/**
 * @author wuhao on 2016/7/16
 */
public class Constants {

	public static final int FIELD_UPDATE_CODE_BASE = 0x10000000;

	public static final ParcelUuid CSR_MESH_SERVICE
			= ParcelUuid.fromString("0000FEF1-0000-1000-8000-00805F9B34FB");

	/**
	 * https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.service.generic_access.xml
	 */
	public static final ParcelUuid GENERIC_ACCESS_PROFILE_SERVICE
			= ParcelUuid.fromString("00001800-0000-1000-8000-00805F9B34FB");

	/**
	 * https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.gap.device_name.xml
	 */
	public static final ParcelUuid GAP_DEVICE_NAME_CHARACTERISTIC
			= ParcelUuid.fromString("00002A00-0000-1000-8000-00805F9B34FB");

	/**
	 * https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.gap.appearance.xml
	 */
	public static final ParcelUuid GAP_APPEARANCE
			= ParcelUuid.fromString("00002A01-0000-1000-8000-00805F9B34FB");

}
