/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.events;

import android.os.Bundle;

/**
 * @author wuhao on 2016/7/30
 */
public class CommunicationResponseEvent {
	private static final String TAG = CommunicationResponseEvent.class.getSimpleName();

	public static final String DEVICE_MAC_ADDRESS = CommunicationResponseEvent.class.getCanonicalName() + ".DEVICE_MAC_ADDRESS";

	public int eventCode;
	private Bundle eventData;

	public CommunicationResponseEvent(int eventCode) {
		this.eventCode = eventCode;
	}

	public CommunicationResponseEvent(CommunicationResponseEvent event) {
		this.eventCode = event.eventCode;
		this.eventData = event.eventData != null ? event.eventData : null;
	}

	public Bundle getEventData() {
		return eventData;
	}

	public void setEventData(Bundle eventData) {
		this.eventData = eventData;
	}
}
