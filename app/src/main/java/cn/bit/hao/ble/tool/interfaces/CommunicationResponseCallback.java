/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.interfaces;

import cn.bit.hao.ble.tool.events.CommunicationResponseEvent;

/**
 * @author wuhao on 2016/7/15
 */
public interface CommunicationResponseCallback {

	public void onCommunicationResponded(CommunicationResponseEvent communicationResponseEvent);
}
