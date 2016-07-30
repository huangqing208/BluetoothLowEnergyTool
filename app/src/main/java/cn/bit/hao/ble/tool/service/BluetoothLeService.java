/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * Gatt通信类
 * 提供数据交互支持
 * 
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 * 
 * @author wuhao
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    
//    private static final BluetoothLeService instance = new BluetoothLeService();

    public static final int STATE_DISCONNECTED = 0x00;
    public static final int STATE_CONNECTING = 0x01;
    public static final int STATE_CONNECTED = 0x02;

    private int mConnectionState = STATE_DISCONNECTED;
    
    private BluetoothManager mBluetoothManager = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothGatt mBluetoothGatt = null;
    private String mBluetoothDeviceAddress = null;

    /**
     * Gatt已连接
     */
    public final static String ACTION_GATT_CONNECTED =
            "com.bugull.btchair.ACTION_GATT_CONNECTED";
    /**
     * Gatt已断开
     */
    public final static String ACTION_GATT_DISCONNECTED =
            "com.bugull.btchair.ACTION_GATT_DISCONNECTED";
    /**
     * 发现Gatt服务
     */
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.bugull.btchair.ACTION_GATT_SERVICES_DISCOVERED";
    /**
     * 有新的返回数据
     */
    public final static String ACTION_DATA_AVAILABLE =
            "com.bugull.btchair.ACTION_DATA_AVAILABLE";
    /**
     * 发送数据成功
     */
    public final static String ACTION_DATA_WRITTEN =
    		"com.bugull.btchair.ACTION_DATA_WRITTEN";
    /**
     * 启用通知的descriptor成功
     */
    public final static String ACTION_DESCRIPTOR_ENABLED =
    		"com.bugull.btchair.ACTION_DESCRIPTOR_ENABLED";
    /**
     * 附加的数据
     */
    public final static String EXTRA_DATA =
            "com.bugull.btchair.EXTRA_DATA";
    /**
     * 附加数据来源的characteristic的UUID
     */
    public final static String EXTRA_CHARACTERISTIC_UUID =
    		"com.bugull.btchair.EXTRA_CHARACTERISTIC_UUID";
    /**
     * 附加数据来源的descriptor的UUID
     */
    public final static String EXTRA_DESCRIPTOR_UUID =
    		"com.bugull.btchair.EXTRA_DESCRIPTOR_UUID";
    
    public int getConnectionState() {
    	return mConnectionState;
    }

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                boolean result = mBluetoothGatt.discoverServices();
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + result);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
//            Log.w(TAG, "onServicesDiscovered received: " + status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//                Log.i(TAG, "onCharacteristicRead WTF");
            } else {
                Log.e(TAG, "onCharacteristicRead WTF failed");
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//            Log.i(TAG, "onCharacteristicChanged WTF");
        }

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_WRITTEN, characteristic);
//                Log.i(TAG, "onCharacteristicWrite WTF");
            } else {
                Log.e(TAG, "onCharacteristicWrite WTF failed");
            }
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
            Log.i(TAG, "onDescriptorWrite WTF " + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				broadcastUpdate(ACTION_DESCRIPTOR_ENABLED, descriptor);
			} else {
	            Log.e(TAG, "onDescriptorWrite WTF failed");
			}
		}
		
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
			BluetoothGattDescriptor descriptor) {
        final Intent intent = new Intent(action);
        // For all other profiles, writes the data formatted in HEX.
        // 暂时不必将数据返回，只要告知操作成功即可
        final byte[] data = descriptor.getValue();
        intent.putExtra(EXTRA_CHARACTERISTIC_UUID, new ParcelUuid(descriptor.getCharacteristic().getUuid()));
        intent.putExtra(EXTRA_DESCRIPTOR_UUID, new ParcelUuid(descriptor.getUuid()));
        intent.putExtra(EXTRA_DATA, data);
        sendBroadcast(intent);
	}

	private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        intent.putExtra(EXTRA_CHARACTERISTIC_UUID, new ParcelUuid(characteristic.getUuid()));
        intent.putExtra(EXTRA_DATA, data);
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
    	// 在本项目中，unbind时不需要关闭gatt，close操作需显式调用
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
//        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        if (mBluetoothAdapter == null) {
	        mBluetoothAdapter = mBluetoothManager.getAdapter();
	        if (mBluetoothAdapter == null) {
	            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
	            return false;
	        }
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        //如果目前已有GATT连接或正在连接中，则不允许尝试再连接，即便是连接同一个目标
        if (mConnectionState != STATE_DISCONNECTED) {
            Log.w(TAG, "ConnectionState " + mConnectionState);
        	return false;
        }
        
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        Log.i(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        return true;
    }

    public String getBluetoothDeviceAddress() {
    	return mBluetoothDeviceAddress;
    }
    
    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "disconnect warning BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        //如果调用disconnect之后马上调用close，就可能在BluetoothProfile.STATE_DISCONNECTED
        //返回之前置mBluetoothGatt为null，于是mConnectionState就没有重置为STATE_DISCONNECTED。
        //下次在进来的时候就会惊异的发现：我擦，mConnectionState居然没重置，于是就不连接gatt了
        //按照android的机制，即便是退出app，static变量也不会被清理，这时候得清理后台才能重置static变量
        //对于这种蛋疼的情况，我们表示，static变量必须在不用的时候重置它
        mConnectionState = STATE_DISCONNECTED;
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        mBluetoothDeviceAddress = null;
        mConnectionState = STATE_DISCONNECTED;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
        return true;
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
    										  String descriptorUuid,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(descriptorUuid));
        if (descriptor == null) {
        	return false;
        }
        descriptor.setValue(enabled 
        		? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        		: BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
        return true;
    }

    public boolean writeCharacteristicValue(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        if (mBluetoothGatt == null) {
            Log.w(TAG, "mBluetoothGatt not initialized");
            return false;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < characteristic.getValue().length; ++i) {
        	sb.append(String.format("%02X ", characteristic.getValue()[i]));
        }
        Log.w(TAG, "writeCharacteristicValue  " + sb.toString());
    	mBluetoothGatt.writeCharacteristic(characteristic);
    	return true;
    }
    
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
    
    public BluetoothGattService getGattService(String uuid) {
    	if (mBluetoothGatt == null || uuid == null) {
    		return null;
    	}
    	
    	try {
    		return mBluetoothGatt.getService(UUID.fromString(uuid));
    	} catch (IllegalArgumentException exception) {
    		exception.printStackTrace();
    		return null;
    	}
    }
}