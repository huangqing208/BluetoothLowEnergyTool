package cn.bit.hao.ble.tool.activity;

import android.os.Bundle;

import cn.bit.hao.ble.tool.R;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;

public class MainActivity extends GattCommunicationActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onCommunicationServiceBound() {

	}

	@Override
	public void onCommonResponded(CommonResponseEvent commonResponseEvent) {
		super.onCommonResponded(commonResponseEvent);
	}
}
