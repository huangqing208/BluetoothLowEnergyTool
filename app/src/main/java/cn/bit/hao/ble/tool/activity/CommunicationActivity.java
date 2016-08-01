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

import cn.bit.hao.ble.tool.events.CommunicationResponseEvent;
import cn.bit.hao.ble.tool.events.ResponseEvent;
import cn.bit.hao.ble.tool.service.CommunicationService;


/**
 * @author wuhao on 2016/7/14
 */
public abstract class CommunicationActivity extends BaseActivity {
	private static final String TAG = CommunicationActivity.class.getSimpleName();

	private CommunicationService communicationService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindCommunicationService();
	}

	private void bindCommunicationService() {
		if (communicationService == null) {
			bindService(new Intent(this, CommunicationService.class),
					communicationServiceConnection, Context.BIND_AUTO_CREATE);
		}
	}

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

	/**
	 * 自此开始Activity可以和Service通信
	 */
	protected abstract void onCommunicationServiceBound();

	private void unbindCommunicationService() {
		if (communicationService != null) {
			unbindService(communicationServiceConnection);
			communicationService = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing()) {
			unbindCommunicationService();
		}
	}

	protected boolean writeCommand(String macAddress, byte[] command) {
		if (communicationService == null) {
			return false;
		}
		communicationService.writeCommand(macAddress, command);
		return true;
	}

	@Override
	public void onCommonResponded(ResponseEvent responseEvent) {
		super.onCommonResponded(responseEvent);
		if (responseEvent instanceof CommunicationResponseEvent) {

		}
	}
}
