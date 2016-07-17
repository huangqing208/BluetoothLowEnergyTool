/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.application;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import cn.bit.hao.ble.tool.service.SystemStateService;


/**
 * @author wuhao on 2016/7/16
 */
public class App extends Application {
	private static final String TAG = App.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Application onCreate");
		startService(new Intent(this, SystemStateService.class));
	}
}
