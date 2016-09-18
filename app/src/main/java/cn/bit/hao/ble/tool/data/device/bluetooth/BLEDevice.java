/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.data.device.bluetooth;


import java.util.UUID;

import cn.bit.hao.ble.tool.application.Constants;
import cn.bit.hao.ble.tool.bluetooth.utils.BluetoothUuid;
import cn.bit.hao.ble.tool.protocol.GeneralProtocol;
import cn.bit.hao.ble.tool.response.events.CommunicationResponseEvent;
import cn.bit.hao.ble.tool.response.manager.CommonEventManager;

/**
 * @author wuhao on 2016/7/14
 */
public class BLEDevice {

	protected static final int RESPONSE_CODE_BASE = Constants.FIELD_UPDATE_CODE_BASE + 0x01000000;

	protected String friendlyName;

	protected String macAddress;

	public BLEDevice(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getMacAddress() {
		return macAddress;
	}

	//==============================================================================================
	// 如果UI有对某些属性变化有需求，那么才会有对应的属性代号用于反馈给UI
	// 比如value是UI不敏感信息，则没有对应代号，而friendlyName是UI敏感信息，则有如下代号
	public static final int FRIENDLY_NAME_CODE = RESPONSE_CODE_BASE + 0x001;
	//==============================================================================================

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		if (this.friendlyName != null && this.friendlyName.equals(friendlyName)) {
			return;
		}
		this.friendlyName = friendlyName;
		// 每变化一个状态则发出一次通知，所以，一次返回有可能有多次通知
		CommunicationResponseEvent responseEvent = new CommunicationResponseEvent(macAddress, FRIENDLY_NAME_CODE);
		CommonEventManager.getInstance().sendResponse(responseEvent);
	}

	/**
	 * <p>当接收到返回信息时，交给对应的数据对象来做解析，而数据对象有信息变化的时候，通过Messenger反馈到UI</p>
	 *
	 * @param response 接收到的返回信息
	 * @return 如果数据被处理完则返回true，否则返回false
	 */
	public boolean parse(UUID serviceUuid, UUID characteristicUuid, byte[] response) {
		if (serviceUuid.equals(BluetoothUuid.GENERIC_ACCESS_PROFILE_SERVICE)) {
			if (characteristicUuid.equals(BluetoothUuid.GAP_DEVICE_NAME_CHARACTERISTIC)) {
				setFriendlyName(new String(response));
				return true;
			}
		}

		// 以下是假设的自定义协议，且假设返回了新的name值
		switch (response[0]) {
			case GeneralProtocol.SET_FRIENDLY_CODE: {
				//==================================================================================
				// 具体的解析过程在此

				//==================================================================================
				// 假装friendlyName有变化
				setFriendlyName("A friendly name");
				return true;
			}
			default:
				break;
		}
		return false;
	}
}
