/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.data;

import cn.bit.hao.ble.tool.events.CommunicationResponseEvent;
import cn.bit.hao.ble.tool.manager.CommunicationResponseManager;
import cn.bit.hao.ble.tool.protocol.FirstKindBLEDeviceProtocol;

/**
 * @author wuhao on 2016/7/14
 */
public class FirstKindBLEDevice extends BLEDevice {

	protected static final int FIELD_CHANGED_BASE = BLEDevice.RESPONSE_CODE_BASE + 0x00100000;

	private static final String DEFAULT_NAME = "MainActivityData";

	protected int sonValue;

	public static final int SON_VALUE_CODE = FIELD_CHANGED_BASE + 0x001;

	public FirstKindBLEDevice(String macAddress) {
		super(macAddress);
		super.value = 0;
		super.friendlyName = DEFAULT_NAME;
		this.sonValue = 1;
	}

	public int getSonValue() {
		return sonValue;
	}

	public void setSonValue(int sonValue) {
		if (this.sonValue == sonValue) {
			return;
		}
		this.sonValue = sonValue;
		// 通知UIsonValue变化
		CommunicationResponseManager.getInstance().sendResponse(new CommunicationResponseEvent(SON_VALUE_CODE));
	}

	@Override
	public boolean parse(byte[] response) {
		if (super.parse(response)) {
			return true;
		}
		boolean parseFinished = false;
		switch (response[0]) {
			case FirstKindBLEDeviceProtocol.QUERY_SON_VALUE_CODE: {
				//==================================================================================
				// 子类的解析返回信息的具体代码在此

				//==================================================================================
				// 假装处理sonValue
				setSonValue(110);
				parseFinished = true;
				break;
			}
			case FirstKindBLEDeviceProtocol.QUERY_SON_VALUE_AND_FRIENDLY_NAME_CODE: {
				//==================================================================================
				// 子类的解析返回信息的具体代码在此

				//==================================================================================
				// 假装处理sonValue
				setSonValue(120);
				parseFinished = true;
				break;
			}
			default:
				break;
		}
		return parseFinished;
	}
}
