package cn.bit.hao.ble.tool.activity;

import android.os.Bundle;

import cn.bit.hao.ble.tool.R;

public class MainActivity extends BindCommunicationServiceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onCommunicationServiceBound() {

	}

	@Override
	public void onCommunicationResponded(int filedCode) {

	}
}
