/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt.communication;

import android.bluetooth.BluetoothGatt;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import cn.bit.hao.ble.tool.bluetooth.gatt.BluetoothGattManager;
import cn.bit.hao.ble.tool.response.callbacks.CommonResponseListener;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;

/**
 * 接收来自UI的write命令，将其缓存到队列中按序发出
 * 也接收来自UI的read命令，TODO: 这个需要缓存么？
 *
 * @author wuhao on 2016/8/9
 */
public class GattRequestManager implements CommonResponseListener {
	private static final String TAG = GattRequestManager.class.getSimpleName();

	private static GattRequestManager instance;

	private Queue<SendTask> sendTaskQueue;

	private Handler handler;

	private GattRequestManager() {
		sendTaskQueue = new LinkedList<>();
		handler = new Handler(Looper.getMainLooper());
	}

	public static GattRequestManager getInstance() {
		if (instance == null) {
			instance = new GattRequestManager();
		}
		return instance;
	}

	/**
	 * 将一段指定的信息发送到目标设备去
	 *
	 * @param macAddress 目标设备mac地址
	 * @param command    发送命令内容，长度不可超过20Byte
	 * @return 如果成功加入发送队列返回true，否则返回false
	 */
	public synchronized boolean sendCommand(String macAddress, ParcelUuid characteristic, byte[] command) {
		BluetoothGatt bluetoothGatt = BluetoothGattManager.getInstance().getBluetoothGatt(macAddress);
		if (bluetoothGatt == null) {
			// 如果不存在此连接，表示当前连接故障，无法接受发送任务，但是已接受的任务在恢复连接后会继续发送
			return false;
		}
		if (command.length > 20) {
			// 如果发送内容长度超过可一次write的长度，则拒绝接受此次任务
			return false;
		}
		SendTask newTask = new SendTask(macAddress, characteristic, command);
		if (sendTaskQueue.contains(newTask)) {
			// 如果待发任务列表存在完全相同的任务，则只保留第一个
			// 这样做似乎对于某些情形是不利的，比如每次发送相同的命令，只是为了做一个累加变化，
			// 但是笔者建议，在有上一个累加任务的返回确认时再做下一次的累加任务会比较好
			return false;
		}
		sendTaskQueue.offer(newTask);
		return true;
	}

	@Override
	public void onCommonResponded(CommonResponseEvent commonResponseEvent) {

	}

	private class SendTask {
		public String macAddress;
		public ParcelUuid characteristic;
		public byte[] command;

		public SendTask(String macAddress, ParcelUuid characteristic, byte[] command) {
			this.macAddress = macAddress;
			this.characteristic = characteristic;
			this.command = command;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof SendTask)) {
				return false;
			}
			SendTask task = (SendTask) o;
			return this.macAddress.equals(task.macAddress)
					&& this.characteristic.equals(task.characteristic)
					&& Arrays.equals(this.command, task.command);
		}
	}

}
