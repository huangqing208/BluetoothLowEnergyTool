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
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
 * 管理App同设备的连接，对外支持开始连接、断开连接、提供连接对象等功能
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

	private enum TimeoutType {
		TYPE_SCAN_DEVICE_TIMEOUT,
		TYPE_CONNECT_TIMEOUT,
		TYPE_DISCONNECT_TIMEOUT
	}

	private static final int SCAN_DEVICE_TIMEOUT = 20000;
	private static final int CONNECTING_TIMEOUT = 20000;
	private static final int DISCONNECTING_TIMEOUT = 2000;

	private final Map<String, Map<TimeoutType, Runnable>> timeoutMap;

	private BluetoothGattManager() {
		bluetoothGattMap = new HashMap<>();
		// ConcurrentHashMap可以保证多线程调用同方法时，比如remove方法时，不会都返回value，
		// 而是一个返回value，另一个返回null
		timeoutMap = new HashMap<>();
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
	 * 添加指定设备，当搜索到对应的设备的广播时才会去连接。
	 * 注意：无需预先开启搜索，此方法会自己开启搜索的。
	 *
	 * @param macAddress 目标设备mac地址
	 * @return 如果添加成功则返回true，否则返回false
	 */
	public boolean connectDeviceWhenValid(final String macAddress) {
		if (!BluetoothUtil.checkBluetoothAddress(macAddress)) {
			return false;
		}

		synchronized (bluetoothGattMap) {
			if (bluetoothGattMap.get(macAddress) != null) {
				return false;
			}
			bluetoothGattMap.put(macAddress, null);
		}

		// 因为存在需要搜索连接的设备，所以开启搜索
		BluetoothLeScanManager.getInstance().startLeScan(this);
		// 如果20s还搜不到目标的话，提醒UI
		setTimeout(macAddress, TimeoutType.TYPE_SCAN_DEVICE_TIMEOUT, new Runnable() {
			@Override
			public void run() {
				CommonResponseManager.getInstance().sendResponse(new BluetoothGattEvent(macAddress,
						BluetoothGattEvent.BluetoothGattCode.GATT_SCAN_DEVICE_TIMEOUT));

				// 在接收到设备信号前，且没有显式disconnect的时候，每次超时都提醒
				setTimeout(macAddress, TimeoutType.TYPE_SCAN_DEVICE_TIMEOUT, this, SCAN_DEVICE_TIMEOUT);
			}
		}, SCAN_DEVICE_TIMEOUT);
		return true;
	}

	/**
	 * 建立连接到目标设备的Gatt连接，并且开始管理此Gatt连接。
	 * 注意：此方法返回true并不表示连接已建立，只是表明已尝试连接，在对应系统回调后才能保证连接建立。
	 *
	 * @param macAddress 目标设备mac地址
	 * @return 如果成功尝试连接则返回true，否则返回false
	 */
	private boolean connectDevice(final String macAddress) {
		// 在此加锁可以保证多线程调用不会重复执行
		synchronized (bluetoothGattMap) {
			if (bluetoothGattMap.get(macAddress) != null) {
				// 如果存在对应的Gatt对象，则表示连接已存在
				return false;
			}

			if (!BluetoothStateManager.getInstance().isBluetoothEnabled()) {
				// 如果蓝牙关闭的话，则暂停连接服务
				return false;
			}
			if (!BluetoothUtil.checkBluetoothAddress(macAddress)) {
				return false;
			}
			Context context = getContext();
			if (context == null) {
				return false;
			}

			BluetoothDevice bluetoothDevice = BluetoothUtil.getBluetoothDevice(context, macAddress);
			// We want to directly connect to the device, so we are setting the autoConnect
			// parameter to false.
			BluetoothGatt bluetoothGatt = bluetoothDevice.connectGatt(context, false,
					new BluetoothGattCallbackImpl());
			Log.i(TAG, "Connect new Gatt " + macAddress + " " + bluetoothGatt.toString());
			bluetoothGattMap.put(macAddress, bluetoothGatt);

			// 如果所有Gatt均已建立，那么也就无需继续搜索了
			if (!bluetoothGattMap.containsValue(null)) {
				BluetoothLeScanManager.getInstance().stopLeScan(this);
			}

			// 系统自带的超时检测是30s，可以人为的在应用层加自定义的超时检测，当然，超时设置通常在30s以内
			setTimeout(macAddress, TimeoutType.TYPE_CONNECT_TIMEOUT, new Runnable() {
				@Override
				public void run() {
					CommonResponseManager.getInstance().sendResponse(
							new BluetoothGattEvent(macAddress,
									BluetoothGattEvent.BluetoothGattCode.GATT_CONNECT_TIMEOUT));
				}
			}, CONNECTING_TIMEOUT);

			return true;
		}
	}

	/**
	 * 获取指定的Gatt对象，然后可用Gatt对象做Gatt通信
	 *
	 * @param macAddress 目标对象的mac地址
	 * @return 对应的Gatt对象，如果返回为null，表示Gatt连接不存在
	 */
	/*package*/ BluetoothGatt getBluetoothGatt(String macAddress) {
		return bluetoothGattMap.get(macAddress);
	}

	/**
	 * 判断指定的Gatt连接是否已建立
	 *
	 * @param macAddress 目标设备的mac地址
	 * @return 如果Gatt连接已建立则返回true，否则返回false
	 */
	public boolean isDeviceConnected(String macAddress) {
		Context context = getContext();
		return context != null
				&& BluetoothUtil.getBluetoothGattState(context, macAddress)
				== BluetoothAdapter.STATE_CONNECTED;
	}

	/**
	 * 断开和指定设备的Gatt连接。
	 * 注意：返回true只表明执行成功，而要在系统对应回调后才能保证实际意义上的连接断开。
	 * 此方法不建议连续对同一设备执行，不会出错，但不推荐。
	 *
	 * @param macAddress 目标设备的mac地址
	 */
	public void disconnectDevice(final String macAddress) {
		BluetoothGatt bluetoothGatt;
		synchronized (bluetoothGattMap) {
			// 如果没有此设备，则啥也不干
			if (!bluetoothGattMap.containsKey(macAddress)) {
				return;
			}
			// 如果此时设备当前无连接，则remove即可
			bluetoothGatt = bluetoothGattMap.get(macAddress);
			if (bluetoothGatt == null) {
				removeDevice(macAddress);
				return;
			}
		}

		// 给断开连接加个超时检测
		setTimeout(macAddress, TimeoutType.TYPE_DISCONNECT_TIMEOUT, new Runnable() {
			@Override
			public void run() {
				// 如果断开连接超时，则删除设备与连接
				removeDevice(macAddress);
			}
		}, DISCONNECTING_TIMEOUT);

		// 设备是有连接的，那么就断开连接好了
		bluetoothGatt.disconnect();
		Log.i(TAG, "disconnectGatt " + macAddress);
	}

	private void disconnectGatt(String macAddress) {
		synchronized (bluetoothGattMap) {
			BluetoothGatt bluetoothGatt = bluetoothGattMap.get(macAddress);
			if (bluetoothGatt != null) {
				bluetoothGatt.disconnect();
			}
		}
	}

	/**
	 * 断开和目标设备的Gatt连接。
	 * 注意：仅仅影响Gatt工作的终结，并不意味着不会再次连接此设备。
	 *
	 * @param macAddress 目标设备的mac地址
	 */
	private void closeGatt(String macAddress) {
		// 置空意味着释放，在别处查询或使用的过程中，如果尽早的知道了被释放其实是OK的，所以不必刻意的加锁
		synchronized (bluetoothGattMap) {
			BluetoothGatt bluetoothGatt = bluetoothGattMap.get(macAddress);
			if (bluetoothGatt == null) {
				return;
			}

			// 将对应的目标设备的gatt设为null，意味着将会再次连接此设备
			bluetoothGattMap.put(macAddress, null);
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
	private void removeDevice(String macAddress) {
		synchronized (timeoutMap) {
			Map<TimeoutType, Runnable> tasks = timeoutMap.remove(macAddress);
			if (tasks != null) {
				TimeoutType[] keys = tasks.keySet().toArray(new TimeoutType[0]);
				for (TimeoutType type : keys) {
					handler.removeCallbacks(tasks.remove(type));
				}
			}
		}

		synchronized (bluetoothGattMap) {
			closeGatt(macAddress);
			bluetoothGattMap.remove(macAddress);

			// 如果所有Gatt均已建立，那么也就无需继续搜索了
			if (!bluetoothGattMap.containsValue(null)) {
				BluetoothLeScanManager.getInstance().stopLeScan(this);
			}
		}

		GattRequestManager.getInstance().removeRequestQueue(macAddress);
	}

	/**
	 * 调用此方法可以结束所有工作，和initManager应当结对出现
	 */
	public void finish() {
		synchronized (bluetoothGattMap) {
			// 这个写法坦白说，没在网上找到雷同的做法，但是删除多个item亦妥妥滴
			// 当然，因为其实是多次单个删除，并非迭代器，效率什么的不清楚
			// 自然无法保证线程安全，必须加synchronized
			// 感觉相比起流行写法来说，这种写法更简洁
			String[] macAddresses = bluetoothGattMap.keySet().toArray(new String[0]);
			for (String macAddress : macAddresses) {
				disconnectGatt(macAddress);
				removeDevice(macAddress);
			}
		}

		CommonResponseManager.getInstance().removeTaskCallback(this);
		applicationContext = null;
	}

	private void setTimeout(String macAddress, final TimeoutType type, final Runnable task, long delay) {
		synchronized (timeoutMap) {
			final Map<TimeoutType, Runnable> tasks;
			if (timeoutMap.get(macAddress) == null) {
				tasks = new ConcurrentHashMap<>();
				timeoutMap.put(macAddress, tasks);
			} else {
				tasks = timeoutMap.get(macAddress);
			}

			// 万一原来的任务还在，相同的任务不能都留着，留最后一个就好
			if (tasks.get(type) != null) {
				cancelTimeout(macAddress, type);
			}

			Runnable taskWrapper = new Runnable() {
				@Override
				public void run() {
					// 用过之后需要丢弃
					tasks.remove(type);
					task.run();
				}
			};
			tasks.put(type, taskWrapper);
			handler.postDelayed(taskWrapper, delay);
		}
	}

	private Runnable cancelTimeout(String macAddress, TimeoutType type) {
		synchronized (timeoutMap) {
			Map<TimeoutType, Runnable> tasks = timeoutMap.get(macAddress);
			if (tasks == null) {
				return null;
			}
			Runnable task = tasks.remove(type);
			if (task != null) {
				handler.removeCallbacks(task);
			}
			return task;
		}
	}

	private void onBluetoothStateOn() {
		// TODO: 尝试重新连接gatt，还是先检测有广播了，再重连？
//		// 方案1：直接重连
//		synchronized (bluetoothGattMap) {
//			String[] keys = bluetoothGattMap.keySet().toArray(new String[0]);
//			for (String key : keys) {
//				if (bluetoothGattMap.get(key) == null) {
//					try {
//						connectDevice(key);
//					} catch (IllegalStateException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
		// 方案2：开启搜索
		// 在少数手机上，有时候当手机刚切换为开启状态时立即startLeScan是不会出错但也不会搜索的。
		// 原因不详，只知道加延时有效。
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				synchronized (bluetoothGattMap) {
					String[] macAddresses = bluetoothGattMap.keySet().toArray(new String[0]);
					for (String macAddress : macAddresses) {
						if (bluetoothGattMap.get(macAddress) == null) {
							connectDeviceWhenValid(macAddress);
						}
					}
				}
			}
		}, 500);
	}

	private void onBluetoothStateOff() {
		synchronized (bluetoothGattMap) {
			// 断开所有蓝牙连接，恢复连接后再重连
			String[] macAddresses = bluetoothGattMap.keySet().toArray(new String[0]);
			for (String macAddress : macAddresses) {
				closeGatt(macAddress);
			}
		}
	}

	private void onGattConnectionError(String macAddress) {
		// 如果是被动的断开了连接或者是连接超时，考虑重新连接
		closeGatt(macAddress);
		// TODO: 是否立即重连，还是检测有广播了再重连？
//        // 方案1：立即重连
//		connectDevice(macAddress);
		// 方案2：开启搜索
		connectDeviceWhenValid(macAddress);
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
		} else if (commonResponseEvent instanceof BluetoothLeScanResultEvent) {
			// 如果搜索到需要重连的设备，那就去重连
			String macAddress = ((BluetoothLeScanResultEvent) commonResponseEvent).getMacAddress();
			synchronized (bluetoothGattMap) {
				if (!bluetoothGattMap.containsKey(macAddress)) {
					return;
				}
				if (bluetoothGattMap.get(macAddress) == null) {
					cancelTimeout(macAddress, TimeoutType.TYPE_SCAN_DEVICE_TIMEOUT);
					connectDevice(macAddress);
				}
			}
		} else if (commonResponseEvent instanceof BluetoothGattEvent) {
			String macAddress = ((BluetoothGattEvent) commonResponseEvent).getMacAddress();
			switch (((BluetoothGattEvent) commonResponseEvent).getEventCode()) {
				case GATT_CONNECTED:
					// 这个连接并不指的是连接建立的时候，而是连接并且查询到服务列表的时候
					cancelTimeout(macAddress, TimeoutType.TYPE_CONNECT_TIMEOUT);
					break;
				case GATT_DISCONNECTED:
					Runnable task = cancelTimeout(macAddress, TimeoutType.TYPE_DISCONNECT_TIMEOUT);
					if (task != null) {
						// 如果是主动发起的断开连接，则再次彻底删除连接
						removeDevice(macAddress);
					} else {
						// 如果是被动的断开了连接的话，则处理方式与以下情形相同
						onGattConnectionError(macAddress);
					}
					break;
				case GATT_REMOTE_DISAPPEARED:
					// 如果超过距离断开连接了的话，那么，主动恢复连接
				case GATT_CONNECTION_ERROR:
					// 意外的错误处理
				case GATT_CONNECT_TIMEOUT:
					// 此时是有尝试连接设备的，先用disconnect取消掉尝试连接，然后再次close并connect
					// TODO: 之所以先取消尝试连接，再重置连接，与固件端程序未优化有一定关系。
					// 具体来说是这么回事：手机端重复尝试连接，然后多次匆匆close并connect时，固件端会因为一些
					// 意想不到的逻辑而认为自己已连接，于是停止广播，手机端故而无法获得BLE端广播。所以解决办法
					// 有二：一是手机端显式取消尝试连接，令BLE获悉；二是BLE端也必须得做到检测长时间无连接需要
					// 恢复状态（可连接并广播状态）
					disconnectGatt(macAddress);
					onGattConnectionError(macAddress);
					break;
				default:
					break;
			}
		}
	}
}
