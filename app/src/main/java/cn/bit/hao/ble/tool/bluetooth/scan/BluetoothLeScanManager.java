/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.scan;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.bit.hao.ble.tool.bluetooth.state.BluetoothStateManager;
import cn.bit.hao.ble.tool.bluetooth.utils.BluetoothUtil;
import cn.bit.hao.ble.tool.response.callbacks.CommonResponseListener;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothLeScanEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothLeScanResultEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothStateEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * @author wuhao on 2016/8/3
 */
public class BluetoothLeScanManager implements CommonResponseListener {
	private static final String TAG = BluetoothLeScanManager.class.getSimpleName();

	private WeakReference<Context> applicationContext;

	private static BluetoothLeScanManager instance;

	/**
	 * 搜索状态，与请求状态独立，与内部工作状态相关。
	 * 逻辑实现会基于搜索状态和请求状态来协调工作。
	 */
	private boolean isLeScanning = false;

	/**
	 * 请求状态，与搜索状态独立，与外部请求相关。
	 * 逻辑实现会基于搜索状态和请求状态来协调工作。
	 */
	private List<CommonResponseListener> scanListeners;

	private LeScanCallbackImpl leScanCallback;
	private ScanCallbackImpl scanCallback;

	private Handler handler;
	private static final int LE_SCAN_TIMEOUT = 30 * 1000;

	private BluetoothLeScanManager() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			leScanCallback = new LeScanCallbackImpl();
		} else {
			scanCallback = new ScanCallbackImpl();
		}
		handler = new Handler(Looper.getMainLooper());
		scanListeners = new ArrayList<>();
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
		if (getContext() != null) {
			// 不接受重复初始化
			return false;
		}
		applicationContext = new WeakReference<Context>(context.getApplicationContext());

		CommonResponseManager.getInstance().addTaskCallback(instance);

		// 假使上次finish无能将leScanCallback停止掉，那么这里必须先停止掉，才能确保之后startLeScan无虞
		scanListeners.clear();
		if (isLeScanning) {
			performStopLeScan();
		}
		return true;
	}

	/**
	 * finish方法和initManager方法成对使用，不可单一多次调用
	 */
	public void finish() {
		handler.removeCallbacks(scanTimeout);
		scanListeners.clear();
		if (isLeScanning) {
			// 必须停止搜索
			performStopLeScan();
		}

		CommonResponseManager.getInstance().removeTaskCallback(this);
		applicationContext = null;
	}

	private Context getContext() {
		return applicationContext == null ? null : applicationContext.get();
	}

	/**
	 * 开启Le搜索
	 * 注意，此方法会保证尽力搜索，即，在蓝牙开启的时候且有监听者时基本保证在搜索
	 *
	 * @return 成功执行的话返回true，否则返回false
	 */
	public synchronized boolean startLeScan(CommonResponseListener listener) {
		if (listener == null || scanListeners.contains(listener)) {
			return false;
		}
		scanListeners.add(listener);
		performStartLeScan();
		return true;
	}

	private boolean performStartLeScan() {
		if (isLeScanning) {
			// 在stopLeScan前不允许重复startLeScan避免触发SCAN_FAILED_ALREADY_STARTED
			return true;
		}
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
			bluetoothAdapter.startLeScan(leScanCallback);
			isLeScanning = true;
		} else {
			BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
			if (bluetoothLeScanner != null) {
				ScanSettings settings = new ScanSettings.Builder()
						.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
				bluetoothLeScanner.startScan(null, settings, scanCallback);
				isLeScanning = true;
			}
		}
		if (isLeScanning) {
			// 无法检测是否真的start成功，在此做应用层超时检测
			Log.i(TAG, "start scanning.");
			handler.removeCallbacks(scanTimeout);
			handler.postDelayed(scanTimeout, LE_SCAN_TIMEOUT);
		}
		return isLeScanning;
	}

	private Runnable scanTimeout = new Runnable() {
		@Override
		public void run() {
			handler.postDelayed(this, LE_SCAN_TIMEOUT);
			CommonResponseManager.getInstance().sendResponse(
					new BluetoothLeScanEvent(BluetoothLeScanEvent.BluetoothLeScanCode.LE_SCAN_TIMEOUT));
		}
	};

	/**
	 * 停止Le搜索
	 *
	 * @param listener 监听者虽然是从{@link CommonResponseManager}接收返回的，但是此Manager需要做统计，方便得悉监听者数量
	 */
	public synchronized void stopLeScan(CommonResponseListener listener) {
		if (listener != null) {
			scanListeners.remove(listener);
		}
		if (scanListeners.size() == 0) {
			performStopLeScan();
		}
	}

	private void performStopLeScan() {
		if (!BluetoothStateManager.getInstance().isBluetoothEnabled()) {
			// 如果在禁用蓝牙的时候调用了stopLeScan，那么应该中止后续的重连尝试
			return;
		}
		Context context = getContext();
		if (context == null) {
			return;
		}
		BluetoothAdapter bluetoothAdapter = BluetoothUtil.getBluetoothAdapter(context);
		if (bluetoothAdapter == null) {
			return;
		}
		handler.removeCallbacks(scanTimeout);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			bluetoothAdapter.stopLeScan(leScanCallback);
			isLeScanning = false;
		} else {
			BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
			if (bluetoothLeScanner != null) {
				bluetoothLeScanner.stopScan(scanCallback);
				isLeScanning = false;
			}
		}
		Log.i(TAG, "stop scanning.");
	}

	@Override
	public void onCommonResponded(CommonResponseEvent commonResponseEvent) {
		if (commonResponseEvent instanceof BluetoothStateEvent) {
			switch (((BluetoothStateEvent) commonResponseEvent).getEventCode()) {
				case BLUETOOTH_STATE_ON:
					// 如果此前isLeScanning为true，表示之前是在搜索中的，那么这里得终止上次搜索
					// 这么做是因为从源码可知，只有蓝牙开着的时候stop动作才有效，才能注销callback
					// 而之所以注销callback是因为蓝牙关闭的时候会中止一切动作，开启后却不会恢复
					if (isLeScanning) {
						performStopLeScan();
					}
					// 在少数手机上，有时候当手机刚切换为开启状态时立即startLeScan是不会出错但也不会搜索的。原因不详，只知道加延时有效。
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if (scanListeners.size() > 0) {
								// 谁知道500ms会发生什么事情，不一定非得执行
								performStartLeScan();
							}
						}
					}, 500);
					break;
				case BLUETOOTH_STATE_OFF:
					// when bluetooth off, scan is passively stopped
					handler.removeCallbacks(scanTimeout);
					break;
				case BLUETOOTH_STATE_ERROR:
					break;
				default:
					break;
			}
		} else if (commonResponseEvent instanceof BluetoothLeScanResultEvent) {
			// 搜索到设备时，认为蓝牙确实已经在工作了
			handler.removeCallbacks(scanTimeout);
		}
	}
}
