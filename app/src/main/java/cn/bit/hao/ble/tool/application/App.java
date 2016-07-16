/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.application;

import android.app.Application;
import android.content.Intent;

import cn.bit.hao.ble.tool.service.SystemStateService;


/**
 * @author wuhao on 2016/7/16
 */
public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		startService(new Intent(this, SystemStateService.class));
	}
}
