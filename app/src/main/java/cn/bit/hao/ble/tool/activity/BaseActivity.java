/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

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
		Log.i(TAG, BaseActivity.this.toString() + ": " + Thread.currentThread().getId());
		BluetoothStateManager.getInstance().addUICallback(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, BaseActivity.this.toString() + ": onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, BaseActivity.this.toString() + ": onPause");
	}

	@Override
	public void onBluetoothActionHappened(int actionCode) {
		// toast or something here
		Toast.makeText(BaseActivity.this, "Bluetooth ooh", Toast.LENGTH_SHORT).show();
	}
}
