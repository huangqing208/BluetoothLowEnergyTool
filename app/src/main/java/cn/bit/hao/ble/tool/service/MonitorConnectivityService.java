package cn.bit.hao.ble.tool.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import cn.bit.hao.ble.tool.manager.BluetoothStateManager;

/**
 * 此Service用于监听设备蓝牙开关状态，也可以按需添加其他状态监听
 */
public class MonitorConnectivityService extends Service {
	private static final String TAG = MonitorConnectivityService.class.getSimpleName();

	public MonitorConnectivityService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		initBluetoothMonitor();
	}

	private BluetoothBroadcastReceiver bluetoothBroadcastReceiver;

	private void initBluetoothMonitor() {
		if (!BluetoothStateManager.getInstance(this).isBluetoothSupported()) {
			stopSelf();
			return;
		}

		if (bluetoothBroadcastReceiver == null) {
			bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
			registerReceiver(bluetoothBroadcastReceiver, getBluetoothBroadcastIntentFilter());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (bluetoothBroadcastReceiver != null) {
			unregisterReceiver(bluetoothBroadcastReceiver);
			bluetoothBroadcastReceiver = null;
		}
	}

	private class BluetoothBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				return;
			}

			final int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			BluetoothStateManager.getInstance(MonitorConnectivityService.this).setBluetoothState(newState);

			// 以下为可配置选项，默认由此Service维护蓝牙重启，也可以禁用此处的功能，在BluetoothCallback回调时在考虑是否重启
			if (newState == BluetoothAdapter.STATE_OFF) {
				// request for enabling bluetooth
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				// 非Activity的context若要startActivity需要添加以下Flag
				enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(enableBtIntent);
			}
		}
	}

	private IntentFilter getBluetoothBroadcastIntentFilter() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		return intentFilter;
	}

}
