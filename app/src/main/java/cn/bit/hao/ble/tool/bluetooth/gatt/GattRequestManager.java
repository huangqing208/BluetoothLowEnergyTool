/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cn.bit.hao.ble.tool.bluetooth.utils.BluetoothUuid;

/**
 * 接收来自UI的各种命令，将其缓存到队列中按序发出
 * 注意：此中的方法在有Gatt连接的时候才能调用
 *
 * @author wuhao on 2016/8/9
 */
public class GattRequestManager {
	private static final String TAG = GattRequestManager.class.getSimpleName();

	private static GattRequestManager instance;

	/**
	 * 为每一个连接维护一个请求队列，同一连接的请求任务是同步执行的，而不同连接的请求任务是异步执行的
	 * 注意：同一连接的不同Characteristic的请求任务也是同步执行的
	 * 详见{@link BluetoothGatt}中mDeviceBusy的使用
	 */
	private Map<String, GattRequestQueue> requestQueues;

	private GattRequestManager() {
		requestQueues = new HashMap<>();
	}

	public static GattRequestManager getInstance() {
		if (instance == null) {
			instance = new GattRequestManager();
		}
		return instance;
	}

	/**
	 * 把任务添加到相对应的请求队列
	 *
	 * @param macAddress  目标设备mac地址
	 * @param requestTask 请求任务
	 * @param prior       是否是优先任务，优先任务队列会先于普通任务队列执行
	 * @return 如果添加成功则返回true，否则返回false
	 */
	private boolean addGattRequestTask(String macAddress, GattRequestTask requestTask, boolean prior) {
		GattRequestQueue requestQueue = requestQueues.get(macAddress);
		if (requestQueue == null) {
			requestQueue = new GattRequestQueue(macAddress);
			requestQueues.put(macAddress, requestQueue);
		}
		boolean result = prior ? requestQueue.addPriorRequestTask(requestTask)
				: requestQueue.addCommonRequestTask(requestTask);
		if (result) {
			requestQueue.performNextTask();
		}
		return result;
	}

	private BluetoothGattCharacteristic getCharacteristic(String macAddress, UUID serviceUuid,
	                                                      UUID characteristicUuid) {
		BluetoothGatt bluetoothGatt = BluetoothGattManager.getInstance().getBluetoothGatt(macAddress);
		if (bluetoothGatt == null) {
			// 如果不存在此连接，表示当前连接故障，无法接受发送任务，但是已接受的任务在恢复连接后会继续发送
			return null;
		}
		BluetoothGattService gattService = bluetoothGatt.getService(serviceUuid);
		if (gattService == null) {
			return null;
		}
		return gattService.getCharacteristic(characteristicUuid);
	}

	/**
	 * 设置Characteristic的Notification/Indication功能
	 *
	 * @param macAddress       目标设备mac地址
	 * @param service          目标Service UUID
	 * @param characteristic   目标Characteristic UUID
	 * @param notificationType 通知的类型，Notification或Indication
	 * @param enable           true表示启用功能，false表示停用功能
	 * @return 如果成功加入请求队列返回true，否则返回false
	 */
	public synchronized boolean setCharacteristicNotification(String macAddress, UUID service, UUID characteristic,
	                                                          GattNotificationTask.NotificationType notificationType,
	                                                          boolean enable) {
		BluetoothGattCharacteristic gattCharacteristic = getCharacteristic(macAddress, service,
				characteristic);
		if (gattCharacteristic == null) {
			return false;
		}

		int gattCharacteristicProperties = gattCharacteristic.getProperties();
		if ((gattCharacteristicProperties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
			// 如果指定的characteristic不支持Notification的话，返回false
			return false;
		}

		BluetoothGattDescriptor gattDescriptor = gattCharacteristic.getDescriptor(
				BluetoothUuid.CLIENT_CHARACTERISTIC_CONFIGURATION);
		if (gattDescriptor == null) {
			return false;
		}

		GattRequestTask newTask = new GattNotificationTask(macAddress, service,
				characteristic, notificationType, enable);
		return addGattRequestTask(macAddress, newTask, true);
	}

	/**
	 * 将一段指定的信息发送到目标Characteristic
	 *
	 * @param macAddress         目标设备mac地址
	 * @param serviceUuid        目标Service UUID
	 * @param characteristicUuid 目标Characteristic UUID
	 * @param content            发送命令内容，长度不可超过20Byte
	 * @param writeType          详见{@link BluetoothGattCharacteristic#setWriteType(int)}
	 * @return 如果成功加入请求队列返回true，否则返回false
	 */
	public synchronized boolean writeCharacteristic(String macAddress, UUID serviceUuid,
	                                                UUID characteristicUuid, byte[] content,
	                                                int writeType) {
		// 如果发送内容长度超过可一次write的长度，则拒绝接受此次任务
		if (content == null || content.length > 20) {
			return false;
		}

		BluetoothGattCharacteristic gattCharacteristic = getCharacteristic(macAddress, serviceUuid,
				characteristicUuid);
		if (gattCharacteristic == null) {
			return false;
		}

		int gattCharacteristicProperties = gattCharacteristic.getProperties();
		// 如果指定的characteristic不支持对应的写类型的话，返回false
		switch (writeType) {
			case BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT:
				if ((gattCharacteristicProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) {
					return false;
				}
				break;
			case BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE:
				if ((gattCharacteristicProperties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0) {
					return false;
				}
				break;
			case BluetoothGattCharacteristic.WRITE_TYPE_SIGNED:
				if ((gattCharacteristicProperties & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) == 0) {
					return false;
				}
				break;
			default:
				// 不支持的写类型，返回false
				return false;
		}

		GattRequestTask newTask = new GattWriteTask(macAddress, serviceUuid,
				characteristicUuid, writeType, content);
		return addGattRequestTask(macAddress, newTask, false);
	}

	public synchronized boolean readCharacteristic(String macAddress, UUID serviceUuid,
	                                               UUID characteristicUuid) {
		BluetoothGattCharacteristic gattCharacteristic = getCharacteristic(macAddress, serviceUuid,
				characteristicUuid);
		if (gattCharacteristic == null) {
			return false;
		}

		int gattCharacteristicProperties = gattCharacteristic.getProperties();
		if ((gattCharacteristicProperties & BluetoothGattCharacteristic.PROPERTY_READ) == 0) {
			return false;
		}

		GattRequestTask newTask = new GattReadTask(macAddress, serviceUuid,
				characteristicUuid);
		return addGattRequestTask(macAddress, newTask, false);
	}

	/**
	 * 在连接建立或恢复时，调用此方法可以恢复原先的Indication?Notification设定并继续暂停的任务队列
	 *
	 * @param macAddress 目标设备mac地址
	 */
	/*package*/ void resumeRequest(String macAddress) {
		GattRequestQueue requestQueue = requestQueues.get(macAddress);
		if (requestQueue != null) {
			requestQueue.performNextTask();
		}
	}

	/**
	 * 如果是连接出现问题的话，需要暂停请求队列的工作
	 *
	 * @param macAddress 目标设备mac地址
	 */
	/*package*/ void pauseRequest(String macAddress) {
		GattRequestQueue requestQueue = requestQueues.get(macAddress);
		if (requestQueue != null) {
			requestQueue.stopPerform();
		}
	}

	/**
	 * 当确认任务执行结果的时候，调用此方法可以继续处理下一个任务
	 * TODO: 这里的处理不知道行不行，因为没有判断当前返回和当前任务是否一致
	 *
	 * @param macAddress 目标设备mac地址
	 * @param result     当前任务执行结果
	 */
	/*package*/ void confirmRequest(String macAddress, boolean result) {
		GattRequestQueue requestQueue = requestQueues.get(macAddress);
		if (requestQueue != null) {
			if (result) {
				requestQueue.confirmTask();
			} else {
				requestQueue.stopPerform();
			}
		}
	}

	/**
	 * 当不再与此设备通信时，将对应的请求队列停止并删除掉
	 *
	 * @param macAddress 目标设备mac地址
	 */
	/*package*/ void removeRequestQueue(String macAddress) {
		GattRequestQueue requestQueue = requestQueues.get(macAddress);
		if (requestQueue != null) {
			requestQueue.clearTasks();
		}
		requestQueues.remove(macAddress);
	}

}
