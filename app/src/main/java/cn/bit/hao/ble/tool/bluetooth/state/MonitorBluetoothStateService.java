package cn.bit.hao.ble.tool.bluetooth.state;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

/**
 * 此Service用于监听设备蓝牙开关状态，也可以按需添加其他状态监听
 */
public class MonitorBluetoothStateService extends Service {
	private static final String TAG = MonitorBluetoothStateService.class.getSimpleName();

	public MonitorBluetoothStateService() {
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
		if (bluetoothBroadcastReceiver == null) {
			bluetoothBroadcastReceiver = new BluetoothBroadcastReceiver();
			registerReceiver(bluetoothBroadcastReceiver, getBluetoothBroadcastIntentFilter());
		}
	}

	private IntentFilter getBluetoothBroadcastIntentFilter() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		return intentFilter;
	}

	private class BluetoothBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				return;
			}

			final int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			BluetoothStateManager.getInstance().setBluetoothState(newState);
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

}
