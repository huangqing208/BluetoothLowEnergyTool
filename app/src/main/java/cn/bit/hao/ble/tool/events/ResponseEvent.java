/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.events;

import android.os.Bundle;

/**
 * @author wuhao on 2016/8/1
 */
public class ResponseEvent implements Cloneable {
	private static final String TAG = ResponseEvent.class.getSimpleName();

	protected Bundle eventData;

	public static final String EXTRA_DEVICE_MAC_ADDRESS = CommunicationResponseEvent.class.getCanonicalName() + ".EXTRA_DEVICE_MAC_ADDRESS";

	public ResponseEvent() {
	}

	public ResponseEvent(ResponseEvent responseEvent) {
		this.eventData = (responseEvent.eventData != null
				? new Bundle(responseEvent.eventData)
				: null);
	}

	public Bundle getEventData() {
		return eventData;
	}

	public void setEventData(Bundle eventData) {
		this.eventData = eventData;
	}

	/**
	 * 想要实现clone的子类也需要实现此方法并拷贝自己的成员对象
	 */
	public ResponseEvent clone() {
		ResponseEvent result = null;
		try {
			result = (ResponseEvent) super.clone();
			result.eventData = (this.eventData != null
					? new Bundle(this.eventData)
					: null);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		} catch (ClassCastException e2) {
			e2.printStackTrace();
		}
		return result;
	}
}
