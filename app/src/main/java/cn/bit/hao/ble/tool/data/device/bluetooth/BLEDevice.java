/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.data.device.bluetooth;


import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.UUID;

import cn.bit.hao.ble.tool.BR;
import cn.bit.hao.ble.tool.bluetooth.utils.BluetoothUuid;
import cn.bit.hao.ble.tool.protocol.GeneralProtocol;

/**
 * @author wuhao on 2016/7/14
 */
public class BLEDevice extends BaseObservable {

	protected String friendlyName;

	protected String macAddress;

	public BLEDevice(String macAddress) {
		this.macAddress = macAddress;
	}

	public String getMacAddress() {
		return macAddress;
	}

	@Bindable
	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		if (this.friendlyName != null && this.friendlyName.equals(friendlyName)) {
			return;
		}
		this.friendlyName = friendlyName;
		notifyPropertyChanged(BR.friendlyName);
	}

	/**
	 * <p>当接收到返回信息时，交给对应的数据对象来做解析，而数据对象有信息变化的时候，通过Messenger反馈到UI</p>
	 *
	 * @param response 接收到的返回信息
	 * @return 如果数据被处理完则返回true，否则返回false
	 */
	public boolean parse(UUID serviceUuid, UUID characteristicUuid, byte[] response) {
		if (serviceUuid.equals(BluetoothUuid.GENERIC_ACCESS_PROFILE_SERVICE)) {
			if (characteristicUuid.equals(BluetoothUuid.GAP_DEVICE_NAME_CHARACTERISTIC)) {
				setFriendlyName(new String(response));
				return true;
			}
		}

		// 以下是假设的自定义协议，且假设返回了新的name值
		switch (response[0]) {
			case GeneralProtocol.SET_FRIENDLY_CODE: {
				//==================================================================================
				// 具体的解析过程在此

				//==================================================================================
				// 假装friendlyName有变化
				setFriendlyName("A friendly name");
				return true;
			}
			default:
				break;
		}
		return false;
	}
}
