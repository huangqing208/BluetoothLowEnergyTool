/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.bit.hao.ble.tool.bluetooth.gatt.communication.GattResponseManager;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothGattEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * 此为自定义的Gatt回调实现
 * TODO: 此类中需要做定制处理，比如有以STATE_CONNECTED为连接标志的，也有以onServicesDiscovered为标志的，等等
 *
 * @author wuhao on 2016/8/1
 */
public class BluetoothGattCallbackImpl extends BluetoothGattCallback {
	private static final String TAG = BluetoothGattCallbackImpl.class.getSimpleName();

	private static final int GATT_CONNECT_TIMEOUT = 133;
	private static final int REMOTE_DISAPPEARED = 8;

	private Handler handler;

	private enum TaskId {
		DISCOVER_SERVICE
	}

	private static final int DISCOVER_SERVICE_TIMEOUT = 15000;

	private Map<TaskId, Runnable> timeoutMap;

	public BluetoothGattCallbackImpl() {
		handler = new Handler(Looper.getMainLooper());
		timeoutMap = new ConcurrentHashMap<>();
	}

	private void setTimeout(final TaskId taskId, final Runnable task, long delay) {
		synchronized (timeoutMap) {
			if (timeoutMap.get(taskId) != null) {
				cancelTimeout(taskId);
			}
			Runnable taskWrapper = new Runnable() {
				@Override
				public void run() {
					timeoutMap.remove(taskId);
					task.run();
				}
			};
			timeoutMap.put(taskId, taskWrapper);
			handler.postDelayed(taskWrapper, delay);
		}
	}

	private void cancelTimeout(TaskId taskId) {
		synchronized (timeoutMap) {
			Runnable task = timeoutMap.remove(taskId);
			if (task != null) {
				handler.removeCallbacks(task);
			}
		}
	}

	/**
	 * 禁断的代码，特殊情形下使用
	 */
	private void refreshCache(BluetoothGatt bluetoothGatt) {
		try {
			Method refresh = bluetoothGatt.getClass().getMethod("refresh", (Class[]) null);
			refresh.invoke(bluetoothGatt, (Object[]) null);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
		final String macAddress = gatt.getDevice().getAddress();
		Log.w(TAG, macAddress + " onConnectionStateChange status: " + status
				+ " newState: " + newState);
		switch (status) {
			case BluetoothGatt.GATT_SUCCESS:
				switch (newState) {
					case BluetoothGatt.STATE_CONNECTED:
						// 绝大情形不可调用以下代码
//						refreshCache(gatt);
						// TODO: 大部分实现中需要搜索Services，当然，按需也可以不做以下搜索
						// Get all the available services. This allows us to query them later.
						// The result of this being successful will be a call to onServicesDiscovered().
						// Don't tell the handler that we are connected until the services have been discovered.
						gatt.discoverServices();
						setTimeout(TaskId.DISCOVER_SERVICE, new Runnable() {
							@Override
							public void run() {
								// 此处算是借用GATT_CONNECTION_ERROR来驱动后续的断开重连
								CommonResponseManager.getInstance().sendResponse(
										new BluetoothGattEvent(macAddress,
												BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTION_ERROR));
							}
						}, DISCOVER_SERVICE_TIMEOUT);
						break;
					case BluetoothGatt.STATE_DISCONNECTED:
						// 不一定是主动断开，可能是被动断开，但却是正常的被动断开
						CommonResponseManager.getInstance().sendResponse(
								new BluetoothGattEvent(macAddress,
										BluetoothGattEvent.BluetoothGattCode.GATT_DISCONNECTED));
						break;
					default:
//						super.onConnectionStateChange(gatt, status, newState);
						CommonResponseManager.getInstance().sendResponse(
								new BluetoothGattEvent(macAddress,
										BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTION_ERROR));
						break;
				}
				break;
			case GATT_CONNECT_TIMEOUT:
				// 通常这是因为连接超时
				CommonResponseManager.getInstance().sendResponse(
						new BluetoothGattEvent(macAddress,
								BluetoothGattEvent.BluetoothGattCode.GATT_CONNECT_TIMEOUT));
				break;
			case REMOTE_DISAPPEARED:
				CommonResponseManager.getInstance().sendResponse(
						new BluetoothGattEvent(macAddress,
								BluetoothGattEvent.BluetoothGattCode.GATT_REMOTE_DISAPPEARED));
				break;
			case BluetoothGatt.GATT_FAILURE:
			default:
				CommonResponseManager.getInstance().sendResponse(
						new BluetoothGattEvent(macAddress,
								BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTION_ERROR));
				break;
		}
	}

	@Override
	public void onServicesDiscovered(BluetoothGatt gatt, int status) {
		super.onServicesDiscovered(gatt, status);
		cancelTimeout(TaskId.DISCOVER_SERVICE);
		if (status != BluetoothGatt.GATT_SUCCESS) {
			// 如果在此得知搜索服务操作失败的话，可以立即反馈，无需超时处理
			// 此处的处理方式是假装连接失败，之后会断开重连重搜索服务的
			// TODO: 如果设备故障，可能会陷入死循环
			CommonResponseManager.getInstance().sendResponse(
					new BluetoothGattEvent(gatt.getDevice().getAddress(),
							BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTION_ERROR));
		} else {
			CommonResponseManager.getInstance().sendResponse(
					new BluetoothGattEvent(gatt.getDevice().getAddress(),
							BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTED));
		}
	}

	@Override
	public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
	                              int status) {
		super.onDescriptorWrite(gatt, descriptor, status);
		if (status != BluetoothGatt.GATT_SUCCESS) {
			// TODO: 如果在此得知某个write操作失败的话，可以立即反馈，无需超时处理
			return;
		}
		// 通常来说，执行到这里多半是和Notification/Indication设置有关
	}

	@Override
	public void onCharacteristicWrite(BluetoothGatt gatt,
	                                  BluetoothGattCharacteristic characteristic, int status) {
		super.onCharacteristicWrite(gatt, characteristic, status);
		if (status != BluetoothGatt.GATT_SUCCESS) {
			// TODO: 如果在此得知某个write操作失败的话，可以立即反馈，无需超时处理
			return;
		}
		// write动作最好是确认一个成功再执行下一个
	}

	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
			characteristic) {
		super.onCharacteristicChanged(gatt, characteristic);
		GattResponseManager.getInstance().receiveResponse(gatt.getDevice().getAddress(),
				characteristic);
	}

	@Override
	public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
	                                 int status) {
		super.onCharacteristicRead(gatt, characteristic, status);
		if (status != BluetoothGatt.GATT_SUCCESS) {
			// TODO: 如果在此得知某个read操作失败的话，可以立即反馈，无需超时处理

			return;
		}
		GattResponseManager.getInstance().receiveResponse(gatt.getDevice().getAddress(),
				characteristic);
	}

}
