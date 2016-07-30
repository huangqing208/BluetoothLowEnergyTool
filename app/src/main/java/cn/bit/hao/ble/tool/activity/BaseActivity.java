/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.activity;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import cn.bit.hao.ble.tool.events.BluetoothEvent;
import cn.bit.hao.ble.tool.interfaces.BluetoothCallback;
import cn.bit.hao.ble.tool.manager.BluetoothStateManager;

/**
 * @author wuhao on 2016/7/16
 */
public abstract class BaseActivity extends AppCompatActivity implements BluetoothCallback {
	private static final String TAG = BaseActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//        refreshBluetoothState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshBluetoothState();
	}

	/**
	 * 按需可在onCreate或onResume调用此方法
	 */
	private void refreshBluetoothState() {
		if (BluetoothStateManager.getInstance().isBluetoothSupported()) {
			// 在添加callback之前查询状态，这是最合理的逻辑顺序了
			BluetoothStateManager.getInstance().addUICallback(this);
		} else {
			showDialog("Not support1!");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing()) {
			// 如果正在退出此Activity，那么应该结束此Activity的业务逻辑，即此后Activity不接收会导致UI变化的状态更新
			// 如果在onDestory中再remove此Callback的话，会影响到后一个Callback的业务逻辑，所以必须尽早remove
			BluetoothStateManager.getInstance().removeUICallback(this);
		}
	}

	@Override
	public void onBluetoothActionHappened(BluetoothEvent bluetoothEvent) {
		switch (bluetoothEvent.eventCode) {
			case BLUETOOTH_NOT_SUPPORTED_CODE: {
				// recommend to ask for exit since no bluetooth supported
				showDialog("Not support2!");
				break;
			}
			case STATE_CHANGED_CODE: {
				if (bluetoothEvent.getEventData().getInt(BluetoothEvent.NEW_BLUETOOTH_STATE) == BluetoothAdapter.STATE_OFF) {
					// if bluetooth is off, show some info
				}
				break;
			}
			case STATE_ERROR_CODE:
			default:
				break;
		}
	}

	//=============Test Code Below=================================
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
