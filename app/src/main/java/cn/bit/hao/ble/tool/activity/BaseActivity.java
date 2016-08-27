/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import cn.bit.hao.ble.tool.bluetooth.scan.BluetoothLeScanManager;
import cn.bit.hao.ble.tool.bluetooth.state.BluetoothStateManager;
import cn.bit.hao.ble.tool.bluetooth.utils.BluetoothUtil;
import cn.bit.hao.ble.tool.response.callbacks.CommonResponseListener;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothLeScanEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothStateEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * 此类为所有Activity的基类，保障所有Activity需要实现的功能。
 * 注意：需根据不同项目要求做定制化调整。
 * 为了降低UI处理，并提高效率，在UI不显示时是不接收回调的，所以要求子类在onStart或onResume时按需刷新UI。
 *
 * @author wuhao on 2016/7/16
 */
public abstract class BaseActivity extends AppCompatActivity implements CommonResponseListener {
	private static final String TAG = BaseActivity.class.getSimpleName();

	private static final int REQUEST_LE_SCAN_PERMISSION = 0x0001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, BaseActivity.this.getClass().getSimpleName() + " onCreate");
		// 先注册回调，再刷新UI，即可确保信息显示及时不丢失
		CommonResponseManager.getInstance().addUICallback(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, BaseActivity.this.getClass().getSimpleName() + " onStart");
		CommonResponseManager.getInstance().registerUINotification(this);
		refreshBluetoothState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, BaseActivity.this.getClass().getSimpleName() + " onResume");
	}

	/**
	 * 按需可在onCreate或onResume调用此方法
	 */
	private void refreshBluetoothState() {
		if (!BluetoothStateManager.getInstance().isBluetoothSupported()) {
			showDialog("Not support!");
		} else if (!BluetoothStateManager.getInstance().isBluetoothEnabled()) {
			BluetoothUtil.requestBluetooth(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, BaseActivity.this.getClass().getSimpleName() + " onPause");
		if (isFinishing()) {
			// Activity间的切换时的生命周期顺序是：退出Activity的onPause->进入Activity的onStart->进入Activity的onResume->退出Activity的onStop。
			// 如果进入Activity在onStart中有逻辑请求，而退出Activity还没有removeUICallback时，返回的消息会送到退出Activity，于是就会出错。
			// 所以，退出Activity在onStop中做removeUICallback是不合理的，在onPause中做removeUICallback才行。
			// 也可以理解为：终结活动的Activity需要立即结束回调监听
			CommonResponseManager.getInstance().removeUICallback(this);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, BaseActivity.this.getClass().getSimpleName() + " onStop");
		// 如果是切换到其他App，则需要停止UI监听
		CommonResponseManager.getInstance().unregisterUINotification(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, BaseActivity.this.getClass().getSimpleName() + " onDestroy");
	}

	@Override
	public void onCommonResponded(CommonResponseEvent commonResponseEvent) {
		if (commonResponseEvent instanceof BluetoothStateEvent) {
			switch (((BluetoothStateEvent) commonResponseEvent).getEventCode()) {
				case BLUETOOTH_STATE_ON:
					break;
				case BLUETOOTH_STATE_OFF:
					// if bluetooth is off, show some info
					break;
				case BLUETOOTH_STATE_ERROR:
				default:
					break;
			}
		} else if (commonResponseEvent instanceof BluetoothLeScanEvent) {
			switch (((BluetoothLeScanEvent) commonResponseEvent).getBluetoothLeScanCode()) {
				case LE_SCAN_TIMEOUT:
					// TODO: show a dialog ask for reset bluetooth
					Toast.makeText(this, "resetting bluetooth...", Toast.LENGTH_SHORT).show();
					BluetoothStateManager.getInstance().resetBluetooth();
					break;
				case LE_SCAN_PERMISSION_DENIED:
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
								Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LE_SCAN_PERMISSION);
					}
					break;
				case LE_SCAN_FAILED:
				default:
					break;
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_LE_SCAN_PERMISSION:
				boolean granted = permissions.length > 0;
				for (int i = 0; i < permissions.length && i < grantResults.length; i++) {
					if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
						granted = false;
						break;
					}
				}
				if (granted) {
					BluetoothLeScanManager.getInstance().startLeScan(null);
				}
				break;
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
				break;
		}
	}

	//=============Test Code Start=================================
	private void showDialog(String content) {
		TextView textView = new TextView(this);
		textView.setPadding(200, 100, 200, 100);
		textView.setText(content);
		Dialog dialog = new Dialog(this);
		dialog.setContentView(textView);
		dialog.show();
	}
	//=============Test Code End===================================
}
