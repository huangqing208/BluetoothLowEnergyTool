/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.events;

/**
 * 通信中的返回事件，通常包含目标设备的mac地址和设备对象被关注的字段的代号
 *
 * @author wuhao on 2016/7/30
 */
public class CommunicationResponseEvent extends ResponseEvent {
	private static final String TAG = CommunicationResponseEvent.class.getSimpleName();

	public int eventCode;

	public CommunicationResponseEvent(int eventCode) {
		super();
		this.eventCode = eventCode;
	}

	public CommunicationResponseEvent(CommunicationResponseEvent communicationResponseEvent) {
		super(communicationResponseEvent);
		this.eventCode = communicationResponseEvent.eventCode;
	}

	@Override
	public CommunicationResponseEvent clone() {
		CommunicationResponseEvent result = null;
		try {
			result = (CommunicationResponseEvent) super.clone();
			result.eventCode = this.eventCode;
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return result;
	}
}
