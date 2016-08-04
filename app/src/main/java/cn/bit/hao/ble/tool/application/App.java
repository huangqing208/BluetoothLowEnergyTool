/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.application;

import android.app.Application;
import android.util.Log;

import cn.bit.hao.ble.tool.bluetooth.discovery.BluetoothLeScanManager;
import cn.bit.hao.ble.tool.bluetooth.gatt.BluetoothGattManager;
import cn.bit.hao.ble.tool.bluetooth.state.BluetoothStateManager;


/**
 * 需要使用的功能需要在Application中初始化，并在退出Application的時候按需处理
 *
 * @author wuhao on 2016/7/16
 */
public class App extends Application {
	private static final String TAG = App.class.getSimpleName();

	private static App instance;

	public static App getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Application onCreate");
		if (instance == null) {
			instance = this;

			// stateManage init时会尝试广播状态，所以置于首位可以确保其他对象不会接收到，事后可以自己按需查询
			BluetoothStateManager.getInstance().initManager(this);
			BluetoothLeScanManager.getInstance().initManager(this);
			BluetoothGattManager.getInstance().initManager(this);

		}
	}

	public void exitApp() {
		if (instance != null) {
			BluetoothGattManager.getInstance().removeAllGatts();
			BluetoothLeScanManager.getInstance().finish();
			BluetoothStateManager.getInstance().finish();

			instance = null;

			// maybe some delay here, or not?
			System.exit(0);
		}
	}
}
