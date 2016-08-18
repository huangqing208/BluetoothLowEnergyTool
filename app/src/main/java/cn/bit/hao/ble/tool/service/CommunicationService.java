package cn.bit.hao.ble.tool.service;

import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import cn.bit.hao.ble.tool.bluetooth.gatt.BluetoothGattManager;
import cn.bit.hao.ble.tool.bluetooth.gatt.GattRequestManager;
import cn.bit.hao.ble.tool.bluetooth.scan.BluetoothLeScanManager;
import cn.bit.hao.ble.tool.bluetooth.utils.BluetoothUuid;
import cn.bit.hao.ble.tool.response.callbacks.CommonResponseListener;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothGattEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * 此Service是用于通信的后台Service，负责处理来自UI的通信请求
 * 在整个应用程序中，主要分为逻辑、数据和UI三部分，此Service是介于UI和逻辑的桥梁
 */
public class CommunicationService extends Service implements CommonResponseListener {
	private static final String TAG = CommunicationService.class.getSimpleName();

	private LocalBinder localBinder;

	public CommunicationService() {
		localBinder = new LocalBinder();
		CommonResponseManager.getInstance().addTaskCallback(this);
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
		CommonResponseManager.getInstance().removeTaskCallback(this);
	}

	/**
	 * 开启蓝牙搜索
	 * 蓝牙搜索结果会由CommonResponseManager返回BluetoothLeScanResultEvent或BluetoothLeScanEvent
	 */
	public void startLeScan() {
		// 忽视返回值，因为关系不大
		BluetoothLeScanManager.getInstance().startLeScan(this);
	}

	/**
	 * 关闭蓝牙搜索
	 */
	public void stopLeScan() {
		BluetoothLeScanManager.getInstance().stopLeScan(this);
	}

	/**
	 * 尝试连接到目标设备
	 * 连接成功的时候会由CommonResponseManager返回BluetoothGattEvent（GATT_CONNECTED）
	 *
	 * @param macAddress 目标设备mac地址
	 * @return 如果成功执行返回true，否则返回false
	 */
	public boolean connectDevice(String macAddress) {
//		BluetoothGattManager.getInstance().connectDevice();
		return BluetoothGattManager.getInstance().connectDeviceWhenValid(macAddress);
	}

	/**
	 * 断开和目标设备的连接
	 *
	 * @param macAddress 目标设备mac地址
	 */
	public void disconnectDevice(String macAddress) {
		BluetoothGattManager.getInstance().disconnectDevice(macAddress);
	}

	/**
	 * 向目标Characteristic发送数据
	 * TODO: 以下实现中的UUID是示例，实际中需要修改
	 *
	 * @param macAddress 目标设备Mac地址
	 * @param content    发送不超过20B的数据
	 * @return 执行成功则返回true，否则返回false
	 */
	public boolean writeCharacteristic(String macAddress, byte[] content) {
		return GattRequestManager.getInstance().writeCharacteristic(macAddress,
				BluetoothUuid.GENERIC_ACCESS_PROFILE_SERVICE,
				BluetoothUuid.GAP_DEVICE_NAME_CHARACTERISTIC,
				content, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
	}

	/**
	 * 请求读取目标数据
	 * 读取到的数据会通知到在GattResponseManager注册的Listener
	 * TODO: 以下实现中的UUID是示例，实际中需要修改
	 *
	 * @param macAddress 目标设备mac地址
	 * @return 执行成功返回true，否则返回false
	 */
	public boolean readCharacteristic(String macAddress) {
		return GattRequestManager.getInstance().readCharacteristic(macAddress,
				BluetoothUuid.GENERIC_ACCESS_PROFILE_SERVICE,
				BluetoothUuid.GAP_DEVICE_NAME_CHARACTERISTIC);
	}

	@Override
	public void onCommonResponded(CommonResponseEvent commonResponseEvent) {
		if (commonResponseEvent instanceof BluetoothGattEvent) {
			String macAddress = ((BluetoothGattEvent) commonResponseEvent).getMacAddress();
			switch (((BluetoothGattEvent) commonResponseEvent).getEventCode()) {
				case GATT_CONNECTED:
					// TODO: If needed, setNotification here!!!
					break;
				default:
					break;
			}
		}
	}

}
