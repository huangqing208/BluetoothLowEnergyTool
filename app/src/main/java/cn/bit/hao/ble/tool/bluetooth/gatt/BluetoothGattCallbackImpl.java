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
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothGattEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * 此为自定义的Gatt回调实现
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

	@Override
	public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
		final String macAddress = gatt.getDevice().getAddress();

		// 但凡是有连接状态改变，都影响请求队列执行，都停止就好了
		GattRequestManager.getInstance().pauseRequest(macAddress);

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
		final String macAddress = gatt.getDevice().getAddress();
		cancelTimeout(TaskId.DISCOVER_SERVICE);
		if (status != BluetoothGatt.GATT_SUCCESS) {
			// 如果在此得知搜索服务操作失败的话，可以立即反馈，无需超时处理
			// 此处的处理方式是假装连接失败，之后会断开重连重搜索服务的
			// TODO: 如果设备故障，可能会陷入死循环
			CommonResponseManager.getInstance().sendResponse(new BluetoothGattEvent(macAddress,
					BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTION_ERROR));
		} else {
			CommonResponseManager.getInstance().sendResponse(new BluetoothGattEvent(macAddress,
					BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTED));

			// 连接建立好的时候，恢复请求任务
			GattRequestManager.getInstance().resumeRequest(macAddress);
		}
	}

	@Override
	public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
	                              int status) {
		super.onDescriptorWrite(gatt, descriptor, status);
		final String macAddress = gatt.getDevice().getAddress();
		// 通常来说，执行到这里多半是和Notification/Indication设置有关
		GattRequestManager.getInstance().confirmRequest(macAddress,
				status == BluetoothGatt.GATT_SUCCESS);
		if (status != BluetoothGatt.GATT_SUCCESS) {
			// 可能是连接问题，所以尝试重连
			CommonResponseManager.getInstance().sendResponse(new BluetoothGattEvent(macAddress,
					BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTION_ERROR));
		}
	}

	@Override
	public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
		super.onDescriptorRead(gatt, descriptor, status);
		final String macAddress = gatt.getDevice().getAddress();
		// 暂时未读过此属性，以后可能会读
		GattRequestManager.getInstance().confirmRequest(macAddress,
				status == BluetoothGatt.GATT_SUCCESS);
		if (status != BluetoothGatt.GATT_SUCCESS) {
			// 可能是连接问题，所以尝试重连
			CommonResponseManager.getInstance().sendResponse(new BluetoothGattEvent(macAddress,
					BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTION_ERROR));
		}
	}

	@Override
	public void onCharacteristicWrite(BluetoothGatt gatt,
	                                  BluetoothGattCharacteristic characteristic, int status) {
		super.onCharacteristicWrite(gatt, characteristic, status);
		final String macAddress = gatt.getDevice().getAddress();
		// 虽然蓝牙规范中只要求writeType为Default时才有来自Server的response，而write without response是没
		// 有来自Server的response的，但是Android的实现中，对write without response也是会在此有回调的，似乎
		// 只是为了表示单方面发送成功
		GattRequestManager.getInstance().confirmRequest(macAddress,
				status == BluetoothGatt.GATT_SUCCESS);
		if (status != BluetoothGatt.GATT_SUCCESS) {
			// 可能是连接问题，所以尝试重连
			CommonResponseManager.getInstance().sendResponse(new BluetoothGattEvent(macAddress,
					BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTION_ERROR));
		}
	}

	@Override
	public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
	                                 int status) {
		super.onCharacteristicRead(gatt, characteristic, status);
		final String macAddress = gatt.getDevice().getAddress();
		// 向GattRequestManager通知read操作已完成
		GattRequestManager.getInstance().confirmRequest(macAddress,
				status == BluetoothGatt.GATT_SUCCESS);
		if (status == BluetoothGatt.GATT_SUCCESS) {
			// 如果接收成功的话，则将返回结果交由解析处理
			GattResponseManager.getInstance().receiveResponse(macAddress,
					characteristic.getService().getUuid(), characteristic.getUuid(),
					Arrays.copyOf(characteristic.getValue(), characteristic.getValue().length));
		} else {
			// 可能是连接问题，所以尝试重连
			CommonResponseManager.getInstance().sendResponse(new BluetoothGattEvent(macAddress,
					BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTION_ERROR));
		}
	}

	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic
			characteristic) {
		super.onCharacteristicChanged(gatt, characteristic);
		// 无论是Notification还是Indication，统一在此返回。
		// 以下将返回结果交由解析处理。
		GattResponseManager.getInstance().receiveResponse(gatt.getDevice().getAddress(),
				characteristic.getService().getUuid(), characteristic.getUuid(),
				Arrays.copyOf(characteristic.getValue(), characteristic.getValue().length));
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

}
