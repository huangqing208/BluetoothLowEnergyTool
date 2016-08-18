/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt;

import java.util.UUID;

/**
 * @author wuhao on 2016/8/17
 */
public interface GattResponseListener {

	/**
	 * 来自设备的信息处理方法，包括read来的或是notification/indication来的
	 * TODO: 由返回的UUID判断信息是否是可处理的，如果是的话，才允许继续处理
	 *
	 * @param macAddress         目标设备mac地址
	 * @param serviceUuid        对应的Service UUID
	 * @param characteristicUuid 对应的Characteristic UUID
	 * @param content            characteristic值的副本
	 * @return 如果信息被处理则返回true，否则返回false
	 */
	public abstract boolean parseGattResponse(String macAddress, UUID serviceUuid,
	                                          UUID characteristicUuid, byte[] content);
}
