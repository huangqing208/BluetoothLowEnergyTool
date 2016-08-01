/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import cn.bit.hao.ble.tool.callbacks.CommonResponseCallback;
import cn.bit.hao.ble.tool.events.BluetoothStateEvent;
import cn.bit.hao.ble.tool.events.ResponseEvent;
import cn.bit.hao.ble.tool.manager.BluetoothStateManager;
import cn.bit.hao.ble.tool.manager.CommonResponseManager;

/**
 * @author wuhao on 2016/7/16
 */
public abstract class BaseActivity extends AppCompatActivity implements CommonResponseCallback {
	private static final String TAG = BaseActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CommonResponseManager.getInstance().addUICallback(this);
//        refreshBluetoothState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 此后需要重新刷新UI才行，因为此前的状态变化没有接收到
		CommonResponseManager.getInstance().setUINotification(true);
		refreshBluetoothState();
	}

	/**
	 * 按需可在onCreate或onResume调用此方法
	 */
	private void refreshBluetoothState() {
		if (!BluetoothStateManager.getInstance(this).isBluetoothSupported()) {
			showDialog("Not support!");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		CommonResponseManager.getInstance().setUINotification(false);
		if (isFinishing()) {
			// 如果正在退出此Activity，那么应该结束此Activity的业务逻辑，即此后Activity不接收会导致UI变化的状态更新
			// 如果在onDestory中再remove此Callback的话，会影响到后一个Callback的业务逻辑，所以必须尽早remove
			CommonResponseManager.getInstance().removeUICallback(this);
		}
	}

	@Override
	public void onCommonResponded(ResponseEvent responseEvent) {
		if (responseEvent instanceof BluetoothStateEvent) {
			switch (((BluetoothStateEvent) responseEvent).eventCode) {
				case BLUETOOTH_STATE_OFF: {
					// if bluetooth is off, show some info

					break;
				}
				case BLUETOOTH_STATE_ON: {
					break;
				}
				case BLUETOOTH_STATE_ERROR:
				default:
					break;
			}
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
