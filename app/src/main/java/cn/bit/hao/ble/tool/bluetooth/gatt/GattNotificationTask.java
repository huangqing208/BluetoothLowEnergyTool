/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.gatt;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

import cn.bit.hao.ble.tool.bluetooth.utils.BluetoothUuid;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothGattEvent;
import cn.bit.hao.ble.tool.response.manager.CommonEventManager;

/**
 * @author wuhao on 2016/8/13
 */
public class GattNotificationTask extends GattRequestTask {
	private static final String TAG = GattNotificationTask.class.getSimpleName();

	public enum NotificationType {
		NOTIFICATION,
		INDICATION
	}

	private boolean enable = true;
	private NotificationType notificationType;

	public GattNotificationTask(String macAddress, UUID serviceUuid, UUID characteristicUuid,
	                            NotificationType notificationType, boolean enable) {
		super(macAddress, serviceUuid, characteristicUuid);
		this.notificationType = notificationType;
		this.enable = enable;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	@Override
	public boolean execute() {
		BluetoothGatt bluetoothGatt = BluetoothGattManager.getInstance().getBluetoothGatt(macAddress);
		if (bluetoothGatt == null) {
			return false;
		}
		BluetoothGattService service = bluetoothGatt.getService(serviceUuid);
		if (service == null) {
			return false;
		}
		BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
		if (characteristic == null) {
			return false;
		}
		BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
				BluetoothUuid.CLIENT_CHARACTERISTIC_CONFIGURATION);
		if (descriptor == null) {
			return false;
		}

		if (!bluetoothGatt.setCharacteristicNotification(characteristic, enable)) {
			// 看setCharacteristicNotification的源码可知，多半是发生了RemoteException，考虑重连
			CommonEventManager.getInstance().sendResponse(new BluetoothGattEvent(macAddress,
					BluetoothGattEvent.BluetoothGattCode.GATT_CONNECTION_ERROR));
			return false;
		}
		/**
		 * 见{@link BluetoothGatt#writeDescriptor(BluetoothGattDescriptor)}源码，可知descriptor的
		 * writeType是由所属characteristic的writeType决定的。为了得到server对writeDescriptor的response，
		 * 我们这里设置所属的characteristic的writeType为WRITE_TYPE_DEFAULT。
		 */
		characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

		byte[] value;
		if (enable) {
			switch (notificationType) {
				case INDICATION:
					value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
					break;
				case NOTIFICATION:
				default:
					value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
					break;
			}
		} else {
			value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
		}
		descriptor.setValue(value);
		// 通常的，上面若是通过了RemoteException，这里应该不太容易再出现RemoteException，
		// 如果还出现writeDescriptor返回false，很可能是DeviceBusy，所以简单的的返回false即可，表明暂停任务
		return bluetoothGatt.writeDescriptor(descriptor);
	}

	/**
	 * 判断指定任务与当前任务是否是同一characteristic且notificationType相同但是仅enable不同的互斥任务
	 *
	 * @param task 目标任务
	 * @return 如果是互斥任务则返回true，否则返回false
	 */
	public boolean isReverse(GattNotificationTask task) {
		if (this == task) {
			return false;
		}
		return super.equals(task)
				&& this.notificationType == task.notificationType
				&& this.enable != task.enable;
	}

	/**
	 * 获取和当前任务互斥的任务
	 * @return 和当前任务互斥的任务
	 */
	public GattNotificationTask getReverse() {
		return new GattNotificationTask(macAddress, serviceUuid, characteristicUuid,
				notificationType, !enable);
	}

	@Override
	public boolean equals(Object o) {
		boolean result = super.equals(o);
		if (!result) {
			return false;
		}
		if (!(o instanceof GattNotificationTask)) {
			return false;
		}
		GattNotificationTask task = (GattNotificationTask) o;
		return this.notificationType == task.notificationType && this.enable == task.enable;
	}
}
