/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
 * 为了降低UI处理，并提高效率，在UI不显示时是不接收回调的，所以要求子类在onStart或onResume时按需刷新UI。
 *
 * @author wuhao on 2016/7/16
 */
public abstract class BaseActivity extends AppCompatActivity implements CommonResponseCallback {
	private static final String TAG = BaseActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 先注册回调，再刷新UI，即可确保信息显示及时不丢失
		CommonResponseManager.getInstance().addUICallback(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, BaseActivity.this.getClass().getSimpleName() + " onStart");
		CommonResponseManager.getInstance().setUINotification(this);
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
		CommonResponseManager.getInstance().setUINotification(null);
		if (isFinishing()) {
			// Activity间的切换时的生命周期是：退出Activity的onPause，进入Activity的onStart，进入Activity的onResume，退出Activity的onStop。
			// 如果进入Activity在onStart中有逻辑请求，当返回迅速时，如果退出Activity还没有removeUICallback，就会出错。
			// 所以，退出Activity在onStop中做removeUICallback是不合理的，在onPause中做removeUICallback才行。
			// 也可以理解为：终结活动的Activity需要立即结束回调监听
			CommonResponseManager.getInstance().removeUICallback(this);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, BaseActivity.this.getClass().getSimpleName() + " onStop");
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
