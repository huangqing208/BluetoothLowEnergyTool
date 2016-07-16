/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.manager;

import android.bluetooth.BluetoothAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.bit.hao.ble.tool.application.Constants;
import cn.bit.hao.ble.tool.interfaces.BluetoothCallback;

/**
 * @author wuhao on 2016/7/16
 */
public class BluetoothStateManager {

	private static final int RESPONSE_CODE_BASE = Constants.STATE_UPDATE_CODE_BASE + 0x01000000;

	private BluetoothStateManager() {
	}

	private static BluetoothStateManager instance = new BluetoothStateManager();

	public static BluetoothStateManager getInstance() {
		return instance;
	}

	private List<BluetoothCallback> uiCallbacks = new ArrayList<>();

	private int bluetoothState = BluetoothAdapter.ERROR;
	private static final int STATE_CHANGED_CODE = RESPONSE_CODE_BASE + 0x001;
	private static final int STATE_ERROR_CODE = RESPONSE_CODE_BASE + 0x002;

	public void setBluetoothState(int newState) {
		if (bluetoothState == newState) {
			return;
		}
		if (bluetoothState == BluetoothAdapter.ERROR) {
			sendActionCode(STATE_ERROR_CODE);
			return;
		}
		bluetoothState = newState;
		sendActionCode(STATE_CHANGED_CODE);
	}

	/**
	 * <p>返回蓝牙的状态，可能是{@link BluetoothAdapter#getState()}的返回值</p>
	 *
	 * @return 如果返回{@link BluetoothAdapter#ERROR}，表示蓝牙功能异常，否则返回蓝牙状态
	 */
	public int getBluetoothState() {
		return bluetoothState;
	}

	/**
	 * 参考{@link BluetoothAdapter#isEnabled()}
	 * @return true if the local adapter is turned on
	 */
	public boolean isEnabled() {
		return bluetoothState == BluetoothAdapter.STATE_ON;
	}

	public boolean addUICallback(BluetoothCallback callback) {
		if (uiCallbacks.contains(callback)) {
			return false;
		}
		uiCallbacks.add(callback);
		return true;
	}

	public void removeUICallback(BluetoothCallback callback) {
		uiCallbacks.remove(callback);
	}

	private void sendActionCode(int actionCode) {
		int size = uiCallbacks.size();
		if (size > 0) {
			uiCallbacks.get(size - 1).onBluetoothActionHappened(actionCode);
		}
	}

}
