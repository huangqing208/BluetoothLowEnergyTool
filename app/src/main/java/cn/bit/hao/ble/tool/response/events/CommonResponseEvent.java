/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.response.events;

/**
 * @author wuhao on 2016/8/1
 */
public abstract class CommonResponseEvent implements Cloneable {
	private static final String TAG = CommonResponseEvent.class.getSimpleName();

	/**
	 * 想要实现clone的子类也需要实现此方法并拷贝自己的成员对象
	 */
	public CommonResponseEvent clone() {
		CommonResponseEvent result = null;
		try {
			result = (CommonResponseEvent) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return result;
	}

}
