/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.data.device.bluetooth;


import cn.bit.hao.ble.tool.application.Constants;
import cn.bit.hao.ble.tool.protocol.GeneralProtocol;
import cn.bit.hao.ble.tool.protocol.bluetooth.FirstKindBLEDeviceProtocol;
import cn.bit.hao.ble.tool.response.events.CommunicationResponseEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * @author wuhao on 2016/7/14
 */
public abstract class BLEDevice {

	protected static final int RESPONSE_CODE_BASE = Constants.FIELD_UPDATE_CODE_BASE + 0x01000000;

	protected int value;
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
		if (this.friendlyName.equals(friendlyName)) {
			return;
		}
		this.friendlyName = friendlyName;
		// 每变化一个状态则发出一次通知，所以，一次返回有可能有多次通知
		CommunicationResponseEvent responseEvent = new CommunicationResponseEvent(macAddress, FRIENDLY_NAME_CODE);
		CommonResponseManager.getInstance().sendResponse(responseEvent);
	}

	/**
	 * <p>当接收到返回信息时，交给对应的数据对象来做解析，而数据对象有信息变化的时候，通过Messenger反馈到UI</p>
	 *
	 * @param response 接收到的返回信息
	 * @return 如果数据被处理完则返回true，否则返回false
	 */
	public boolean parse(byte[] response) {
		// 假设有条返回同时包含了新的name值和新的son value
		boolean parseFinished = false;
		switch (response[0]) {
			case GeneralProtocol.SET_FRIENDLY_CODE: {
				//==================================================================================
				// 具体的解析过程在此

				//==================================================================================
				// 假装friendlyName有变化
				setFriendlyName("New name");
				parseFinished = true;
				break;
			}
			case FirstKindBLEDeviceProtocol.QUERY_SON_VALUE_AND_FRIENDLY_NAME_CODE: {
				//==================================================================================
				// 具体的解析过程在此

				//==================================================================================
				// 假装friendlyName有变化
				setFriendlyName("New friendly name");
				// 以下这行要不要吧，无所谓
				parseFinished = false;
				break;
			}
			default:
				break;
		}
		return parseFinished;
	}
}
