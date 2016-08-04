/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import cn.bit.hao.ble.tool.bluetooth.state.BluetoothStateManager;
import cn.bit.hao.ble.tool.bluetooth.utils.BluetoothUtil;
import cn.bit.hao.ble.tool.response.callbacks.CommonResponseCallback;
import cn.bit.hao.ble.tool.response.events.BluetoothStateEvent;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;
import cn.bit.hao.ble.tool.response.manager.CommonResponseManager;

/**
 * 此类为所有Activity的基类，保障所有Activity需要实现的功能。
 * 注意：需根据不同项目要求做定制化调整。
 *
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
		if (!BluetoothStateManager.getInstance().isBluetoothSupported()) {
			showDialog("Not support!");
		} else if (!BluetoothStateManager.getInstance().isBluetoothEnabled()) {
			BluetoothUtil.requestBluetooth(this);
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
	public void onCommonResponded(CommonResponseEvent commonResponseEvent) {
		if (commonResponseEvent instanceof BluetoothStateEvent) {
			switch (((BluetoothStateEvent) commonResponseEvent).getEventCode()) {
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
