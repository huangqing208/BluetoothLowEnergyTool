/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import cn.bit.hao.ble.tool.bluetooth.scan.BluetoothLeScanManager;
import cn.bit.hao.ble.tool.bluetooth.state.BluetoothStateManager;
import cn.bit.hao.ble.tool.bluetooth.utils.BluetoothUtil;
import cn.bit.hao.ble.tool.response.callbacks.CommonResponseListener;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothGattEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothLeScanResultEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothStateEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * 管理App同设备的连接
 *
 * @author wuhao on 2016/7/18
 */
public class BluetoothGattManager implements CommonResponseListener {
	private static final String TAG = BluetoothGattManager.class.getSimpleName();

	private WeakReference<Context> applicationContext;

	// 保存当前的各个Gatt连接对象
	private final Map<String, BluetoothGatt> bluetoothGattMap;

	private static BluetoothGattManager instance;

	private Handler handler;

	private static final int DISCONNECTING_TIMEOUT = 2000;

	private BluetoothGattManager() {
		bluetoothGattMap = new HashMap<>();
		handler = new Handler(Looper.getMainLooper());
	}

	public static synchronized BluetoothGattManager getInstance() {
		if (instance == null) {
			instance = new BluetoothGattManager();
		}
		return instance;
	}

	/**
	 * 初始化BluetoothGattManager，具体实现是获取Application上下文对象保证工作正常。
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
		applicationContext = new WeakReference<>(context.getApplicationContext());

		// 确保监听状态变化，可以在蓝牙不工作的时候做出反应
		CommonResponseManager.getInstance().addTaskCallback(instance);
		return true;
	}

	private Context getContext() {
		return (applicationContext == null || applicationContext.get() == null)
				? null
				: applicationContext.get();
	}

	/**
	 * 添加指定设备，当搜索到对应的设备的广播时才会去连接
	 *
	 * @param macAddress 目标设备mac地址
	 * @return 如果添加成功则返回true，否则返回false
	 */
	public boolean connectDeviceWhenValid(String macAddress) {
		if (!BluetoothUtil.checkBluetoothAddress(macAddress)) {
			return false;
		}
		synchronized (bluetoothGattMap) {
			if (bluetoothGattMap.containsKey(macAddress)) {
				return false;
			}
			bluetoothGattMap.put(macAddress, null);
		}
		// 因为存在需要搜索连接的设备，所以开启搜索
		BluetoothLeScanManager.getInstance().startLeScan(this);
		return true;
	}

	/**
	 * 建立连接到目标设备的Gatt连接，并且开始管理此Gatt连接。
	 * 注意：此方法返回true并不表示连接已建立，只是表明已尝试连接，在对应系统回调后才能保证连接建立。
	 *
	 * @param macAddress 目标设备mac地址
	 * @return 如果成功尝试连接则返回true，否则返回false
	 */
	private boolean connectDevice(String macAddress) {
		if (!BluetoothStateManager.getInstance().isBluetoothEnabled()) {
			// 如果蓝牙关闭的话，则暂停连接服务
			return false;
		}
		if (!BluetoothUtil.checkBluetoothAddress(macAddress)) {
			return false;
		}

		// 在此加锁可以保证多线程调用不会重复执行
		synchronized (bluetoothGattMap) {
			if (bluetoothGattMap.get(macAddress) != null) {
				// 如果存在对应的Gatt对象，则表示连接已存在
				return false;
			}

			Context context = getContext();
			if (context == null) {
				return false;
			}
			BluetoothDevice bluetoothDevice = BluetoothUtil.getBluetoothDevice(context, macAddress);

			long time = SystemClock.uptimeMillis();
			// We want to directly connect to the device, so we are setting the autoConnect
			// parameter to false.
			BluetoothGatt bluetoothGatt = bluetoothDevice.connectGatt(context, false, new BluetoothGattCallbackImpl());
			Log.i(TAG, "Connect new Gatt costs " + (SystemClock.uptimeMillis() - time) + " ms");

			bluetoothGattMap.put(macAddress, bluetoothGatt);

			// 如果所有Gatt均已建立，那么也就无需继续搜索了
			if (!bluetoothGattMap.containsValue(null)) {
				BluetoothLeScanManager.getInstance().stopLeScan(this);
			}
			return true;
		}
	}

	/**
	 * 获取指定的Gatt对象，然后可用Gatt对象做Gatt通信
	 *
	 * @param macAddress 目标对象的mac地址
	 * @return 对应的Gatt对象，如果返回为null，表示Gatt连接不存在
	 */
	public BluetoothGatt getBluetoothGatt(String macAddress) {
		return bluetoothGattMap.get(macAddress);
	}

	/**
	 * 获取Gatt连接的状态
	 *
	 * @param macAddress 目标设备的mac地址
	 * @return 如果有和目标设备建立Gatt连接的话，则返回{@link BluetoothAdapter#STATE_CONNECTED}，
	 * 否则返回{@link BluetoothAdapter#STATE_DISCONNECTED}。当context为空时返回-1。
	 */
	public int getBluetoothGattState(String macAddress) {
		Context context = getContext();
		return context == null ? -1 : BluetoothUtil.getBluetoothGattState(context, macAddress);
	}

	/**
	 * 判断指定的Gatt连接是否已建立
	 *
	 * @param macAddress 目标设备的mac地址
	 * @return 如果Gatt连接已建立则返回true，否则返回false
	 */
	public boolean isGattConnected(String macAddress) {
		Context context = getContext();
		return context != null && BluetoothUtil.getBluetoothGattState(context, macAddress) == BluetoothAdapter.STATE_CONNECTED;
	}

	/**
	 * 断开和指定设备的Gatt连接。
	 * 注意：返回true只表明执行成功，而要在系统对应回调后才能保证实际意义上的连接断开。
	 * 此方法不可连续对同一设备执行，不会出错，但不推荐。
	 *
	 * @param macAddress 目标设备的mac地址
	 * @return 如果成功执行的话，则返回true，否则返回false
	 */
	public void disconnectGatt(final String macAddress) {
		BluetoothGatt bluetoothGatt;
		synchronized (bluetoothGattMap) {
			if (!bluetoothGattMap.containsKey(macAddress)) {
				return;
			}
			bluetoothGatt = bluetoothGattMap.get(macAddress);
			if (bluetoothGatt == null) {
				removeGatt(macAddress);
				return;
			}
		}
		if (!isGattConnected(macAddress)) {
			// 如果尝试连接但没连接上的话，直接remove掉就行了
			removeGatt(macAddress);
			return;
		}
		bluetoothGatt.disconnect();
		Log.i(TAG, "disconnectGatt " + macAddress);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// 如果断开连接超时，则手动删除连接
				removeGatt(macAddress);
			}
		}, DISCONNECTING_TIMEOUT);
		return;
	}

	/**
	 * 断开和目标设备的Gatt连接。
	 * 注意：仅仅影响Gatt工作的终结，并不意味着不会再次连接此设备。
	 *
	 * @param macAddress 目标设备的mac地址
	 * @return 被断开的Gatt连接对象
	 */
	private void closeGatt(String macAddress) {
		// 置空意味着释放，在别处查询或使用的过程中，如果尽早的知道了被释放其实是OK的，所以不必刻意的加锁
		synchronized (bluetoothGattMap) {
			BluetoothGatt bluetoothGatt = null;
			if (bluetoothGattMap.containsKey(macAddress)) {
				bluetoothGatt = bluetoothGattMap.get(macAddress);
				if (bluetoothGatt == null) {
					return;
				}
				// 将对应的目标设备的gatt设为null，意味着将会再次连接此设备
				bluetoothGattMap.put(macAddress, null);
			}
			if (bluetoothGatt == null) {
				return;
			}
			// After using a given BLE device, the app must call this method to ensure resources are
			// released properly.
			bluetoothGatt.close();
			Log.i(TAG, "closeGatt " + macAddress);
		}
	}

	/**
	 * 删除Gatt连接。
	 * 注意：彻底删除目标设备的记录，不再尝试重连。
	 *
	 * @param macAddress 目标设备的mac地址
	 */
	private void removeGatt(String macAddress) {
		synchronized (bluetoothGattMap) {
			closeGatt(macAddress);
			bluetoothGattMap.remove(macAddress);

			// 如果所有Gatt均已建立，那么也就无需继续搜索了
			if (!bluetoothGattMap.containsValue(null)) {
				BluetoothLeScanManager.getInstance().stopLeScan(this);
			}
		}
	}

	/**
	 * 调用此方法可以结束所有工作，和initManager应当结对出现
	 */
	public void removeAllGatts() {
		synchronized (bluetoothGattMap) {
			for (String macAddress : bluetoothGattMap.keySet()) {
				removeGatt(macAddress);
			}
		}
		CommonResponseManager.getInstance().removeTaskCallback(this);
		applicationContext = null;
	}

	private boolean discoverServices(String macAddress) {
		BluetoothGatt bluetoothGatt = bluetoothGattMap.get(macAddress);
		if (bluetoothGatt == null) {
			return false;
		}
		// 每次新建的Gatt对象都需要请求一次Services列表才行
		return bluetoothGatt.discoverServices();
	}

	private void onBluetoothStateOn() {
		// TODO: 尝试重新连接gatt，还是先检测有广播了，再重连？
//		// 方案1：直接重连
//		synchronized (bluetoothGattMap) {
//			for (Map.Entry<String, BluetoothGatt> item : bluetoothGattMap.entrySet()) {
//				if (item.getValue() == null) {
//					try {
//						connectDevice(item.getKey());
//					} catch (IllegalStateException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
		// 方案2：开启搜索
		// 在少数手机上，有时候当手机刚切换为开启状态时立即startLeScan是不会出错但也不会搜索的。原因不详，只知道加延时有效。
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				synchronized (bluetoothGattMap) {
					if (bluetoothGattMap.containsValue(null)) {
						BluetoothLeScanManager.getInstance().startLeScan(BluetoothGattManager.this);
					}
				}
			}
		}, 500);
	}

	private void onBluetoothStateOff() {
		synchronized (bluetoothGattMap) {
			// 断开所有蓝牙连接，恢复连接后再重连
			for (String macAddress : bluetoothGattMap.keySet()) {
				closeGatt(macAddress);
			}
		}
	}

	@Override
	public void onCommonResponded(CommonResponseEvent commonResponseEvent) {
		if (commonResponseEvent instanceof BluetoothStateEvent) {
			switch (((BluetoothStateEvent) commonResponseEvent).getEventCode()) {
				case BLUETOOTH_STATE_ON:
					onBluetoothStateOn();
					break;
				case BLUETOOTH_STATE_OFF:
					onBluetoothStateOff();
					break;
				case BLUETOOTH_STATE_ERROR:
				default:
					break;
			}
		} else if (commonResponseEvent instanceof BluetoothGattEvent) {
			String macAddress = ((BluetoothGattEvent) commonResponseEvent).getMacAddress();
			switch (((BluetoothGattEvent) commonResponseEvent).getEventCode()) {
				case GATT_CONNECTED:
					discoverServices(macAddress);
					break;
				case GATT_DISCONNECTED:
					// 如果是主动发起的断开连接，则再次彻底删除连接
					removeGatt(macAddress);
					break;
				case GATT_REMOTE_DISAPPEARED:
				case GATT_CONNECT_TIMEOUT:
					// 如果是被动的断开了连接或者是连接超时，考虑重新连接
					closeGatt(macAddress);
					// TODO: 是否立即重连，还是检测有广播了再重连？
//                  // 方案1：立即重连
//					connectDevice(macAddress);
					// 方案2：开启搜索
					BluetoothLeScanManager.getInstance().startLeScan(this);
					break;
				default:
					break;
			}
		} else if (commonResponseEvent instanceof BluetoothLeScanResultEvent) {
			// 如果搜索到需要重连的设备，那就去重连
			String macAddress = ((BluetoothLeScanResultEvent) commonResponseEvent).getMacAddress();
			synchronized (bluetoothGattMap) {
				if (!bluetoothGattMap.containsKey(macAddress)) {
					return;
				}
				if (bluetoothGattMap.get(macAddress) == null) {
					connectDevice(macAddress);
				}
			}
		}
	}
}
