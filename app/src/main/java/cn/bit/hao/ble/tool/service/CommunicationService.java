package cn.bit.hao.ble.tool.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import cn.bit.hao.ble.tool.manager.BluetoothGattManager;

/**
 * 此Service是用于通信的后台Service，负责处理来自UI的通信请求
 */
public class CommunicationService extends Service {
	private static final String TAG = CommunicationService.class.getSimpleName();

	private LocalBinder localBinder;
	private BluetoothGattManager bluetoothGattManager;

	public CommunicationService() {
		bluetoothGattManager = BluetoothGattManager.getInstance(this);
		localBinder = new LocalBinder();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return localBinder;
	}

	public class LocalBinder extends Binder {
		public CommunicationService getService() {
			return CommunicationService.this;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		bluetoothGattManager.removeAllGatts();
	}

	/**
	 * 异步方法，向目标Characteristic发送数据
	 *
	 * @param macAddress 目标设备Mac地址
	 * @param command    发送不超过20B的数据
	 */
	public void writeCommand(String macAddress, byte[] command) {
	}

	public void readCharacteristic() {
	}

}
