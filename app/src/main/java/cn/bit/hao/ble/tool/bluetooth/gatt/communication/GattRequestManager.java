/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt.communication;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import cn.bit.hao.ble.tool.bluetooth.gatt.BluetoothGattManager;
import cn.bit.hao.ble.tool.bluetooth.gatt.communication.request.GattIndicationRequestTask;
import cn.bit.hao.ble.tool.bluetooth.gatt.communication.request.GattNotificationRequestTask;
import cn.bit.hao.ble.tool.bluetooth.gatt.communication.request.GattRequestTask;
import cn.bit.hao.ble.tool.bluetooth.gatt.communication.request.GattWriteRequestTask;

/**
 * 接收来自UI的write命令，将其缓存到队列中按序发出
 * 也接收来自UI的read命令，TODO: 这个需要缓存么？
 *
 * @author wuhao on 2016/8/9
 */
public class GattRequestManager {
	private static final String TAG = GattRequestManager.class.getSimpleName();

	private static GattRequestManager instance;

	private Queue<GattRequestTask> requestTaskQueue;

	/**
	 * 为每一个连接维护一个请求队列，同一连接的请求任务是同步执行的，而不同连接的请求任务是异步执行的
	 * 注意：同一连接的不同Characteristic的请求任务也是同步执行的
	 * 详见{@link BluetoothGatt}中mDeviceBusy的使用
	 */
	private Map<String, Queue<GattRequestTask>> requestTaskQueues;

	private Handler handler;

	/**
	 * Core 4.2 Vol 3 Part G Section 3.3.3.3 P2228
	 */
	public static final UUID CLIENT_CHARACTERISTIC_CONFIGURATION
			= UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");

	private Boolean mDeviceBusy = false;

	private GattRequestManager() {
		requestTaskQueue = new LinkedList<>();
		handler = new Handler(Looper.getMainLooper());
	}

	public static GattRequestManager getInstance() {
		if (instance == null) {
			instance = new GattRequestManager();
		}
		return instance;
	}

	private BluetoothGattCharacteristic getCharacteristic(String macAddress, UUID service, UUID characteristic) {
		BluetoothGatt bluetoothGatt = BluetoothGattManager.getInstance().getBluetoothGatt(macAddress);
		if (bluetoothGatt == null) {
			// 如果不存在此连接，表示当前连接故障，无法接受发送任务，但是已接受的任务在恢复连接后会继续发送
			return null;
		}
		BluetoothGattService gattService = bluetoothGatt.getService(service);
		if (gattService == null) {
			return null;
		}
		return gattService.getCharacteristic(characteristic);
	}

	private void performNextTask() {
		GattRequestTask task = requestTaskQueue.peek();
		BluetoothGatt bluetoothGatt = BluetoothGattManager.getInstance().getBluetoothGatt(task.getMacAddress());
		if (bluetoothGatt == null) {
			// 如果执行队列任务的时候发现连接消失，应该判断是蓝牙已关还是连接已断，
		}
		if (task instanceof GattNotificationRequestTask) {

		}
	}

	/**
	 * 设置Characteristic的Notification功能
	 *
	 * @param macAddress     目标设备mac地址
	 * @param service        目标Service UUID
	 * @param characteristic 目标Characteristic UUID
	 * @param enable         true表示启用Notification，false表示停用Notification
	 * @return 如果成功加入请求队列返回true，否则返回false
	 */
	public synchronized boolean setNotification(String macAddress, UUID service, UUID characteristic, boolean enable) {
		BluetoothGattCharacteristic gattCharacteristic = getCharacteristic(macAddress, service, characteristic);
		if (gattCharacteristic == null) {
			return false;
		}

		int gattCharacteristicProperties = gattCharacteristic.getProperties();
		if ((gattCharacteristicProperties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
			// 如果指定的characteristic不支持Notification的话，返回false
			return false;
		}

		BluetoothGattDescriptor gattDescriptor = gattCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION);
		if (gattDescriptor == null) {
			return false;
		}

		GattRequestTask newTask = new GattNotificationRequestTask(macAddress, service, characteristic, enable);
		if (requestTaskQueue.contains(newTask)) {
			// 如果待发任务列表存在完全相同的任务，则只保留第一个
			// 这样做似乎对于某些情形是不利的，比如每次发送相同的命令，只是为了做一个累加变化，
			// 但是笔者建议，在有上一个累加任务的返回确认时再做下一次的累加任务会比较好
			return false;
		}

		requestTaskQueue.offer(newTask);
		synchronized (mDeviceBusy) {
			if (mDeviceBusy) {
				return true;
			}
			mDeviceBusy = true;
		}
		performNextTask();
		return true;
	}

	/**
	 * 设置Characteristic的Indication功能
	 *
	 * @param macAddress     目标设备mac地址
	 * @param service        目标Service UUID
	 * @param characteristic 目标Characteristic UUID
	 * @param enable         true表示启用Indication，false表示停用Indication
	 * @return 如果成功加入请求队列返回true，否则返回false
	 */
	public synchronized boolean setIndication(String macAddress, UUID service, UUID characteristic, boolean enable) {
		BluetoothGattCharacteristic gattCharacteristic = getCharacteristic(macAddress, service, characteristic);
		if (gattCharacteristic == null) {
			return false;
		}

		int gattCharacteristicProperties = gattCharacteristic.getProperties();
		if ((gattCharacteristicProperties & BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0) {
			// 如果指定的characteristic不支持Indication的话，返回false
			return false;
		}

		BluetoothGattDescriptor gattDescriptor = gattCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIGURATION);
		if (gattDescriptor == null) {
			return false;
		}

		GattRequestTask newTask = new GattIndicationRequestTask(macAddress, service, characteristic, enable);
		return !requestTaskQueue.contains(newTask) && requestTaskQueue.offer(newTask);
	}

	/**
	 * 将一段指定的信息发送到目标Characteristic
	 *
	 * @param macAddress     目标设备mac地址
	 * @param service        目标Service UUID
	 * @param characteristic 目标Characteristic UUID
	 * @param command        发送命令内容，长度不可超过20Byte
	 * @param writeType      详见{@link BluetoothGattCharacteristic#setWriteType(int)}
	 * @return 如果成功加入请求队列返回true，否则返回false
	 */
	public synchronized boolean sendCommand(String macAddress, UUID service, UUID characteristic,
	                                        byte[] command, int writeType) {
		// 如果发送内容长度超过可一次write的长度，则拒绝接受此次任务
		if (command.length > 20) {
			return false;
		}

		BluetoothGattCharacteristic gattCharacteristic = getCharacteristic(macAddress, service, characteristic);
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

		GattRequestTask newTask = new GattWriteRequestTask(macAddress, service, characteristic, writeType, command);
		// 如果待发任务列表存在完全相同的任务，则只保留第一个
		// 这样做似乎对于某些情形是不利的，比如每次发送相同的命令，只是为了做一个累加变化，
		// 但是笔者建议，在有上一个累加任务的返回确认时再做下一次的累加任务会比较好
		return !requestTaskQueue.contains(newTask) && requestTaskQueue.offer(newTask);
	}


	public enum GattRequestConfirmationType {
		DESCRIPTOR_WRITE,
		DESCRIPTOR_READ,
		CHARACTERISTIC_WRITE,
		CHARACTERISTIC_READ
	}

	public void confirmRequestTask(String macAddress, UUID service, UUID characteristic,
	                               GattRequestConfirmationType confirmationType) {

	}

}
