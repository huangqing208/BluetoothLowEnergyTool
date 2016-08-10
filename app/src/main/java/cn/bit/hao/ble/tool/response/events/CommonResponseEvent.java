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
	 * 如果子类想实现深复制，且包含String和原生数据类型之外的对象成员，则需要实现此方法并深复制那些对象成员
	 */
	@Override
	public CommonResponseEvent clone() {
		CommonResponseEvent result = null;
		try {
			/**
			 * Creates and returns a copy of this {@code Object}. The default
			 * implementation returns a so-called "shallow" copy: It creates a new
			 * instance of the same class and then copies the field values (including
			 * object references) from this instance to the new instance. A "deep" copy,
			 * in contrast, would also recursively clone nested objects. A subclass that
			 * needs to implement this kind of cloning should call {@code super.clone()}
			 * to create the new instance and then create deep copies of the nested,
			 * mutable objects.
			 */
			result = (CommonResponseEvent) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return result;
	}

}
