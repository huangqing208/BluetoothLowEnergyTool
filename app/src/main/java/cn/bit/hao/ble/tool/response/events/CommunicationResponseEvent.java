/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.events;

/**
 * 通信中的返回事件，通常包含目标设备的mac地址和设备对象被关注的字段的代号
 *
 * @author wuhao on 2016/7/30
 */
public class CommunicationResponseEvent extends CommonResponseEvent {
	private static final String TAG = CommunicationResponseEvent.class.getSimpleName();

	private String macAddress;
	private int fieldCode;

	public CommunicationResponseEvent(String macAddress, int eventCode) {
		this.macAddress = macAddress;
		this.fieldCode = eventCode;
	}

	public CommunicationResponseEvent(CommunicationResponseEvent communicationResponseEvent) {
		this.macAddress = communicationResponseEvent.macAddress;
		this.fieldCode = communicationResponseEvent.fieldCode;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public int getFieldCode() {
		return fieldCode;
	}

	public void setFieldCode(int fieldCode) {
		this.fieldCode = fieldCode;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("fieldCode: ").append(fieldCode);
		return sb.toString();
	}
}
