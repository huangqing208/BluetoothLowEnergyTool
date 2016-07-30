package cn.bit.hao.ble.tool.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BluetoothService extends Service {
	private static final String TAG = BluetoothService.class.getSimpleName();

	private LocalBinder localBinder;
	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;

	public class LocalBinder extends Binder {
		public BluetoothService getService() {
			return BluetoothService.this;
		}
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "BluetoothService onCreate");
		super.onCreate();
		if (bluetoothManager == null || bluetoothAdapter == null) {
			bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			bluetoothAdapter = bluetoothManager.getAdapter();
		}
		if (localBinder == null) {
			localBinder = new LocalBinder();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "BluetoothService onBind");
		return localBinder;
	}

	private boolean isBluetoothOn() {
		if (bluetoothAdapter != null) {
			return bluetoothAdapter.isEnabled();
		}
		return false;
	}

	public boolean startLeScan() {

		return true;
	}

}
