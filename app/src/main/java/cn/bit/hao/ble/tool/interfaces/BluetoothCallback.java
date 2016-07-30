/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.interfaces;

import cn.bit.hao.ble.tool.events.BluetoothEvent;

/**
 * @author wuhao on 2016/7/16
 */
public interface BluetoothCallback {

	public abstract void onBluetoothActionHappened(BluetoothEvent bluetoothEvent);
}
