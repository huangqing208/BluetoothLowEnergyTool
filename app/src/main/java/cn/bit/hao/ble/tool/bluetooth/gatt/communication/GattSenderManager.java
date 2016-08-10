/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt.communication;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author wuhao on 2016/8/9
 */
public class GattSenderManager {
	private static final String TAG = GattSenderManager.class.getSimpleName();

	private static GattSenderManager instance;

	private Queue<SendTask> sendTaskQueue;

	private GattSenderManager() {
		sendTaskQueue = new LinkedList<>();
	}

	public static GattSenderManager getInstance() {
		if (instance == null) {
			instance = new GattSenderManager();
		}
		return instance;
	}

	public boolean sendCommand(String macAddress, byte[] command) {
		if (command.length > 20) {
			return false;
		}
		SendTask newTask = new SendTask(macAddress, command);
		if (sendTaskQueue.contains(newTask)) {
			return false;
		}
		sendTaskQueue.offer(newTask);
		return true;
	}

	private class SendTask {
		public String macAddress;
		public byte[] command;

		public SendTask(String macAddress, byte[] command) {
			this.macAddress = macAddress;
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
					&& Arrays.equals(this.command, task.command);
		}
	}

}
