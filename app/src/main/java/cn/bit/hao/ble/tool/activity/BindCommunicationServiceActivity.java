/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import cn.bit.hao.ble.tool.interfaces.CommunicationResponseCallback;
import cn.bit.hao.ble.tool.manager.CommunicationResponseManager;
import cn.bit.hao.ble.tool.service.CommunicationService;


/**
 * @author wuhao on 2016/7/14
 */
public abstract class BindCommunicationServiceActivity extends BaseActivity implements CommunicationResponseCallback {
	private static final String TAG = BindCommunicationServiceActivity.class.getSimpleName();

	protected CommunicationService communicationService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CommunicationResponseManager.getInstance().addUICallback(this);
		bindCommunicationService();
	}

	private void bindCommunicationService() {
		if (communicationService == null) {
			bindService(new Intent(this, CommunicationService.class), communicationServiceConnection, Context.BIND_AUTO_CREATE);
		}
	}

	/**
	 * 自此开始Activity可以和Service通信
	 */
	protected abstract void onCommunicationServiceBound();

	private ServiceConnection communicationServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			communicationService = ((CommunicationService.LocalBinder) service).getService();
			onCommunicationServiceBound();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e(TAG, "MyService crashed!!");
			unbindCommunicationService();

			// rebind service
			bindCommunicationService();
		}
	};

	protected void unbindCommunicationService() {
		if (communicationService != null) {
			unbindService(communicationServiceConnection);
			communicationService = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		CommunicationResponseManager.getInstance().setUINotification(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		CommunicationResponseManager.getInstance().setUINotification(false);
		if (isFinishing()) {
			// 如果正在退出此Activity，那么应该结束此Activity的业务逻辑
			// 即此后Activity不接收会导致UI变化的状态更新
			// 如果在onDestory中再remove此Callback的话，会影响到后一个Callback的业务逻辑，所以必须尽早remove
			unbindCommunicationService();
			CommunicationResponseManager.getInstance().removeUICallback(this);
		}
	}

}
