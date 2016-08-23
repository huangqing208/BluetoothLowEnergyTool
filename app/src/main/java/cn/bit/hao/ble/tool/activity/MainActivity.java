package cn.bit.hao.ble.tool.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bit.hao.ble.tool.R;
import cn.bit.hao.ble.tool.application.App;
import cn.bit.hao.ble.tool.application.Constants;
import cn.bit.hao.ble.tool.bluetooth.scan.BluetoothLeScanManager;
import cn.bit.hao.ble.tool.bluetooth.utils.ScanRecordCompat;
import cn.bit.hao.ble.tool.data.DeviceStore;
import cn.bit.hao.ble.tool.data.device.bluetooth.BLEDevice;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;
import cn.bit.hao.ble.tool.response.events.CommunicationResponseEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothGattEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothLeScanResultEvent;

public class MainActivity extends BleCommunicationActivity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private TextView helloWorld;
	private TextView deviceName;
	private CoordinatorLayout coordinatorLayout;

	private static final String TARGET_DEVICE_ADDRESS = "00:02:5B:00:25:13";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		DeviceStore.getInstance().addDevice(new BLEDevice(TARGET_DEVICE_ADDRESS));

		helloWorld = (TextView) findViewById(R.id.hello_world);
		deviceName = (TextView) findViewById(R.id.device_name);
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
		Snackbar.make(coordinatorLayout, "Just \n Wave", Snackbar.LENGTH_SHORT).setAction("OK",
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Snackbar.make(coordinatorLayout, "Fine", Snackbar.LENGTH_SHORT).show();
					}
				}).setActionTextColor(Color.YELLOW).show();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 界面呈现时，旧数据陈旧可信度不一定高，考虑清空重来
		scanResults.clear();
		deviceName.setText(DeviceStore.getInstance().getDevice(TARGET_DEVICE_ADDRESS).getFriendlyName());
	}

	@Override
	protected void onCommunicationServiceBound() {
		BluetoothLeScanManager.getInstance().startLeScan(this);
//		communicationService.connectDevice(TARGET_DEVICE_ADDRESS);
	}

	@Override
	protected void beforeCommunicationServiceUnbound() {
		BluetoothLeScanManager.getInstance().stopLeScan(this);
//		communicationService.disconnectDevice(TARGET_DEVICE_ADDRESS);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing()) {
			App.getInstance().exitApp();
		}
	}

	private int count;
	private Map<String, ScanRecordCompat> scanResults = new HashMap<>();

	@Override
	public void onCommonResponded(CommonResponseEvent commonResponseEvent) {
		super.onCommonResponded(commonResponseEvent);
		if (commonResponseEvent instanceof BluetoothLeScanResultEvent) {
			helloWorld.setText("Count: " + ++count + "\n" + commonResponseEvent.toString());

			// 收集搜索结果
			ScanRecordCompat scanRecordCompat = ((BluetoothLeScanResultEvent) commonResponseEvent).getScanRecord();
			List<ParcelUuid> serviceList = scanRecordCompat.getServiceUuids();
			if (serviceList != null && serviceList.contains(new ParcelUuid(Constants.CSR_MESH_SERVICE))) {
				String macAddress = ((BluetoothLeScanResultEvent) commonResponseEvent).getDevice().getAddress();
				if (!scanResults.containsKey(macAddress)) {
					scanResults.put(macAddress, scanRecordCompat);
					Log.i(TAG, "mesh device count " + scanResults.size());
				}
			}
		} else if (commonResponseEvent instanceof BluetoothGattEvent) {
			// UI观察GATT连接事件，可以向用户反馈状态变化
			Log.i(TAG, commonResponseEvent.toString());
			String macAddress = ((BluetoothGattEvent) commonResponseEvent).getMacAddress();
			switch (((BluetoothGattEvent) commonResponseEvent).getEventCode()) {
				case GATT_SCAN_DEVICE_TIMEOUT:
					Snackbar.make(coordinatorLayout, "scan timeout", Snackbar.LENGTH_SHORT).show();
					break;
				case GATT_CONNECTED:
					communicationService.readCharacteristic(TARGET_DEVICE_ADDRESS);
					break;
				case GATT_CONNECT_TIMEOUT:
				case GATT_DISCONNECTED:
				case GATT_CONNECTION_ERROR:
				case GATT_REMOTE_DISAPPEARED:
					Snackbar.make(coordinatorLayout, "connection error", Snackbar.LENGTH_SHORT).show();
					break;
				default:
					break;
			}
		} else if (commonResponseEvent instanceof CommunicationResponseEvent) {
			BLEDevice bleDevice = DeviceStore.getInstance().getDevice(
					((CommunicationResponseEvent) commonResponseEvent).getMacAddress());
			if (bleDevice != null) {
				switch (((CommunicationResponseEvent) commonResponseEvent).getFieldCode()) {
					case BLEDevice.FRIENDLY_NAME_CODE:
						deviceName.setText(bleDevice.getFriendlyName());
						break;
					default:
						break;
				}
			}
		}
	}
}
