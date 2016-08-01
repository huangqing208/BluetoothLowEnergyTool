/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.callbacks;

import cn.bit.hao.ble.tool.events.ResponseEvent;

/**
 * @author wuhao on 2016/7/15
 */
public interface CommonResponseCallback {

	/**
	 * 当有事件发生时，统一通过此回调来通知
	 *
	 * @param responseEvent 反馈的事件对象
	 */
	public void onCommonResponded(ResponseEvent responseEvent);
}
