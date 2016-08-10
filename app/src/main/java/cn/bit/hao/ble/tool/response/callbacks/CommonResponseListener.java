/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.callbacks;

import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;

/**
 * @author wuhao on 2016/7/15
 */
public interface CommonResponseListener {

	/**
	 * 当有事件发生时，统一通过此回调来通知
	 *
	 * @param commonResponseEvent 反馈的事件对象
	 */
	public void onCommonResponded(CommonResponseEvent commonResponseEvent);
}
