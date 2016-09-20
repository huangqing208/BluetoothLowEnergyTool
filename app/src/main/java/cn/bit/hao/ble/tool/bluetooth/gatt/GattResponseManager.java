/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt;

import java.util.UUID;

/**
 * 设备返回的接收处理
 * TODO: 此类中处理业务相关的逻辑，所以是需要定制的。
 * TODO: 最好重新设计下此类的工作
 *
 * @author wuhao on 2016/8/11
 */
public class GattResponseManager {
	private static final String TAG = GattResponseManager.class.getSimpleName();

	private static GattResponseManager instance;

	private GattResponseManager() {
	}

	public static synchronized GattResponseManager getInstance() {
		if (instance == null) {
			instance = new GattResponseManager();
		}
		return instance;
	}

	private GattResponseListener gattResponseListener;

	public void setGattResponseListener(GattResponseListener gattResponseListener) {
		this.gattResponseListener = gattResponseListener;
	}

	/**
	 * 接收来自目标characteristic的信息反馈，并转发给
	 *
	 * @param macAddress         目标设备mac地址
	 * @param serviceUuid        对应的service UUID
	 * @param characteristicUuid 对应的characteristic UUID
	 * @param content            characteristic值的副本
	 */
	/*package*/ void receiveResponse(String macAddress, UUID serviceUuid, UUID characteristicUuid, byte[] content) {
		if (gattResponseListener != null) {
			gattResponseListener.parseGattResponse(macAddress, serviceUuid, characteristicUuid, content);
		}
	}

}
