package cn.bit.hao.ble.tool.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;

import cn.bit.hao.ble.tool.manager.BluetoothStateManager;

public class SystemStateService extends Service {
	private static final String TAG = SystemStateService.class.getSimpleName();

	private BluetoothBroadcastReceiver bluetoothBroadcastReceiver;

	public SystemStateService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onCreate() {
		super.onCreate();
//		Log.i(TAG, "SystemStateService: " + Thread.currentThread().getId());
		BluetoothAdapter bluetoothAdapter;
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		} else {
			BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			bluetoothAdapter = bluetoothManager.getAdapter();
		}
		if (bluetoothAdapter == null) {
			stopSelf();
			return;
		}
		// init BluetoothStateManager
		BluetoothStateManager.getInstance().setBluetoothState(bluetoothAdapter.getState());

		if (bluetoothBroadcastReceiver == null) {
			bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
		}
		registerReceiver(bluetoothBroadcastReceiver, getBluetoothBroadcastIntentFilter());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (bluetoothBroadcastReceiver != null) {
			unregisterReceiver(bluetoothBroadcastReceiver);
		}
	}

	private void enableBluetooth() {
		if (!BluetoothStateManager.getInstance().isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			// 非Activity context startActivity需要添加以下Flag
			enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(enableBtIntent);
		}
	}

	private class BluetoothBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				return;
			}
			final int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
//			Log.i(TAG, "BluetoothBroadcastReceiver: " + Thread.currentThread().getId());
			BluetoothStateManager.getInstance().setBluetoothState(newState);
		}
	}

	private IntentFilter getBluetoothBroadcastIntentFilter() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		return intentFilter;
	}
}
