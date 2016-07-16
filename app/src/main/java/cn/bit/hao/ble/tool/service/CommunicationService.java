package cn.bit.hao.ble.tool.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import cn.bit.hao.ble.tool.data.BLEDevice;
import cn.bit.hao.ble.tool.protocol.GeneralProtocol;


public class CommunicationService extends Service {
	private static final String TAG = CommunicationService.class.getSimpleName();

	private LocalBinder localBinder;

	public CommunicationService() {
		localBinder = new LocalBinder();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return localBinder;
	}

	public class LocalBinder extends Binder {
		public CommunicationService getService() {
			return CommunicationService.this;
		}
	}

	public void doSomething(BLEDevice data) {
		data.parse(new byte[]{GeneralProtocol.SET_FRIENDLY_CODE});
	}

	public void parseResponse(BLEDevice data, byte[] response) {
		data.parse(response);
	}

}
