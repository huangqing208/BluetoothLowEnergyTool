/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.state;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;

import cn.bit.hao.ble.tool.bluetooth.utils.BluetoothUtil;
import cn.bit.hao.ble.tool.response.events.BluetoothStateEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * 此Manager的最大作用就是管理蓝牙状态，并且向需要接收状态变化的对象通知状态变化事件。
 * 需要在Application的OnCreate时初始化，且生命周期和Application相同。
 *
 * @author wuhao on 2016/7/16
 */
public class BluetoothStateManager {
	private static final String TAG = BluetoothStateManager.class.getSimpleName();

	private static BluetoothStateManager instance;

	/**
	 * 默认设备支持蓝牙
	 */
	private boolean bluetoothSupported = true;

	private int bluetoothState = BluetoothAdapter.ERROR;

	private WeakReference<Context> applicationContext;

	private BluetoothStateManager() {
	}

	public static synchronized BluetoothStateManager getInstance() {
		if (instance == null) {
			instance = new BluetoothStateManager();
		}
		return instance;
	}

	/**
	 * 初始化BluetoothStateManager
	 *
	 * @param context 上下文对象
	 * @return 如果设备不支持蓝牙，则返回false
	 */
	public boolean initManager(Context context) {
		if (context == null) {
			return false;
		}
		if (applicationContext != null && applicationContext.get() != null) {
			return false;
		}
		applicationContext = new WeakReference<Context>(context.getApplicationContext());

		BluetoothAdapter bluetoothAdapter = BluetoothUtil.getBluetoothAdapter(context.getApplicationContext());
		if (bluetoothAdapter == null) {
			this.bluetoothSupported = false;
			applicationContext = null;
			return false;
		}
		setBluetoothState(bluetoothAdapter.getState());

		// 启动监听服务来保证自己的状态刷新
		context.startService(new Intent(context, MonitorBluetoothStateService.class));
		return true;
	}

	private Context getContext() {
		if (applicationContext == null || applicationContext.get() == null) {
			return null;
		}
		return applicationContext.get();
	}

	/**
	 * 结束自己的工作
	 */
	public void finish() {
		if (!bluetoothSupported) {
			return;
		}
		Context context = getContext();
		if (context != null) {
			// 停止监听服务
			context.stopService(new Intent(context, MonitorBluetoothStateService.class));
		}
		applicationContext = null;
	}

	/**
	 * 获悉设备是否支持蓝牙
	 *
	 * @return 如果设备支持蓝牙则返回true，否则返回false
	 */
	public boolean isBluetoothSupported() {
		return bluetoothSupported;
	}

	/**
	 * 当{@link MonitorBluetoothStateService}监听到状态变化时，调用此方法修改状态。
	 * 而本Manager则会将此事件通知到各个注册着的回调函数
	 *
	 * @param newState 新的蓝牙状态
	 */
	public synchronized void setBluetoothState(int newState) {
		if (bluetoothState == newState) {
			return;
		}
		bluetoothState = newState;
		if (newState == BluetoothAdapter.ERROR) {
			CommonResponseManager.getInstance().sendResponse(new BluetoothStateEvent(
					BluetoothStateEvent.BluetoothStateCode.BLUETOOTH_STATE_ERROR));
			return;
		}
		BluetoothStateEvent event;
		switch (bluetoothState) {
			case BluetoothAdapter.STATE_ON:
				event = new BluetoothStateEvent(BluetoothStateEvent.BluetoothStateCode.BLUETOOTH_STATE_ON);
				break;
			case BluetoothAdapter.STATE_OFF:
				// TODO: 以下为可配置选项，默认由此Service维护蓝牙重启，也可以禁用此处的功能，在BluetoothCallback回调时再考虑是否重启
				Context context = getContext();
				if (context != null) {
					BluetoothUtil.requestBluetooth(context);
				}
				event = new BluetoothStateEvent(BluetoothStateEvent.BluetoothStateCode.BLUETOOTH_STATE_OFF);
				break;
			default:
				event = null;
				break;
		}
		if (event != null) {
			CommonResponseManager.getInstance().sendResponse(event);
		}
	}

	/**
	 * <p>返回蓝牙的状态，可能是{@link BluetoothAdapter#getState()}的返回值</p>
	 *
	 * @return 如果返回{@link BluetoothAdapter#ERROR}，表示设备不支持蓝牙功能，否则返回蓝牙状态
	 */
	public int getBluetoothState() {
		return bluetoothState;
	}

	/**
	 * 获悉蓝牙是否启用，注意：即便是正在启动蓝牙，即{@link BluetoothAdapter#STATE_TURNING_ON}，也会返回false
	 *
	 * @return 如果蓝牙已启用则返回true，否则返回false
	 */
	public boolean isBluetoothEnabled() {
		return isBluetoothSupported() && bluetoothState == BluetoothAdapter.STATE_ON;
	}

}