/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.discovery;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;

import java.lang.ref.WeakReference;

import cn.bit.hao.ble.tool.bluetooth.state.BluetoothStateManager;
import cn.bit.hao.ble.tool.bluetooth.utils.BluetoothUtil;
import cn.bit.hao.ble.tool.response.callbacks.CommonResponseCallback;
import cn.bit.hao.ble.tool.response.events.BluetoothStateEvent;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * @author wuhao on 2016/8/3
 */
public class BluetoothLeScanManager implements CommonResponseCallback {
	private static final String TAG = BluetoothLeScanManager.class.getSimpleName();

	private WeakReference<Context> applicationContext;

	private static BluetoothLeScanManager instance;

	private boolean isLeScanning = false;

	private LeScanCallbackImpl leScanCallback;
	private ScanCallbackImpl scanCallback;

	private BluetoothLeScanManager() {
		leScanCallback = new LeScanCallbackImpl();
		scanCallback = new ScanCallbackImpl();
	}

	public static synchronized BluetoothLeScanManager getInstance() {
		if (instance == null) {
			instance = new BluetoothLeScanManager();
		}
		return instance;
	}

	/**
	 * 初始化BluetoothDiscoveryManager，具体实现是获取Application上下文对象保证工作正常。
	 *
	 * @param context 上下文对象
	 * @return 如果初始化成功则返回true，否则返回false
	 */
	public boolean initManager(Context context) {
		if (context == null) {
			return false;
		}
		if (applicationContext != null && applicationContext.get() != null) {
			return false;
		}
		applicationContext = new WeakReference<Context>(context.getApplicationContext());

		CommonResponseManager.getInstance().addTaskCallback(instance);
		return true;
	}

	public void finish() {
		CommonResponseManager.getInstance().removeTaskCallback(this);
		applicationContext = null;
	}

	private Context getContext() {
		return (applicationContext == null || applicationContext.get() == null)
				? null
				: applicationContext.get();
	}

	public synchronized boolean startLeScan() {
		if (!BluetoothStateManager.getInstance().isBluetoothEnabled()) {
			return false;
		}
		Context context = getContext();
		if (context == null) {
			return false;
		}
		BluetoothAdapter bluetoothAdapter = BluetoothUtil.getBluetoothAdapter(context);
		if (bluetoothAdapter == null) {
			return false;
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			isLeScanning = bluetoothAdapter.startLeScan(leScanCallback);
		} else {
			BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
			if (bluetoothLeScanner != null) {
				ScanSettings settings = new ScanSettings.Builder()
						.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
				bluetoothLeScanner.startScan(null, settings, scanCallback);
			}
		}
		return true;
	}

	public synchronized boolean stopLeScan() {
		if (!BluetoothStateManager.getInstance().isBluetoothEnabled()) {
			return false;
		}
		Context context = getContext();
		if (context == null) {
			return false;
		}
		BluetoothAdapter bluetoothAdapter = BluetoothUtil.getBluetoothAdapter(context);
		if (bluetoothAdapter == null) {
			return false;
		}
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			bluetoothAdapter.stopLeScan(leScanCallback);
		} else {
			BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
			if (bluetoothLeScanner != null) {
				bluetoothLeScanner.stopScan(scanCallback);
			}
		}
		return true;
	}

	@Override
	public void onCommonResponded(CommonResponseEvent commonResponseEvent) {
		if (commonResponseEvent instanceof BluetoothStateEvent) {
			switch (((BluetoothStateEvent) commonResponseEvent).getEventCode()) {
				case BLUETOOTH_STATE_ON:
					break;
				case BLUETOOTH_STATE_OFF:
					// just pause, can't stop now, and can't resume self
					break;
				case BLUETOOTH_STATE_ERROR:
					break;
				default:
					break;
			}
		}
	}
}
