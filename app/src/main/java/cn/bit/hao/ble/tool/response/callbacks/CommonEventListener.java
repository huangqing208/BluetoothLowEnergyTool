/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.callbacks;

import cn.bit.hao.ble.tool.response.events.CommonEvent;

/**
 * @author wuhao on 2016/7/15
 */
public interface CommonEventListener {

	/**
	 * 当有事件发生时，统一通过此回调来通知
	 *
	 * @param commonEvent 反馈的事件对象
	 */
	public void onCommonResponded(CommonEvent commonEvent);
}
