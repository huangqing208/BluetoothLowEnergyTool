package cn.bit.hao.ble.tool.activity;

import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bit.hao.ble.tool.R;
import cn.bit.hao.ble.tool.application.App;
import cn.bit.hao.ble.tool.application.Constants;
import cn.bit.hao.ble.tool.bluetooth.utils.ScanRecordCompat;
import cn.bit.hao.ble.tool.data.DeviceStore;
import cn.bit.hao.ble.tool.data.device.bluetooth.BLEDevice;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;
import cn.bit.hao.ble.tool.response.events.CommunicationResponseEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothGattEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothLeScanResultEvent;

public class MainActivity extends GattCommunicationActivity {
	private static final String TAG = MainActivity.class.getSimpleName();

	private TextView helloWorld;
	private TextView deviceName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		DeviceStore.getInstance().addDevice(new BLEDevice("00:02:5B:00:25:13"));

		helloWorld = (TextView) findViewById(R.id.hello_world);
		deviceName = (TextView) findViewById(R.id.device_name);
	}

	@Override
	protected void onCommunicationServiceBound() {
//		communicationService.startLeScan();
		communicationService.connectDevice("00:02:5B:00:25:13");
	}

	@Override
	protected void beforeCommunicationServiceUnbound() {
		communicationService.stopLeScan();
		communicationService.disconnectDevice("00:02:5B:00:25:13");
	}

	private int count;
	private Map<String, ScanRecordCompat> scanResults = new HashMap<>();

	@Override
	protected void onStart() {
		super.onStart();
		// 界面呈现时，旧数据陈旧可信度不一定高，考虑清空重来
		scanResults.clear();
	}

	@Override
	public void onCommonResponded(CommonResponseEvent commonResponseEvent) {
		super.onCommonResponded(commonResponseEvent);
		if (commonResponseEvent instanceof BluetoothLeScanResultEvent) {
			helloWorld.setText("Count: " + ++count + "\n" + commonResponseEvent.toString());

			// 收集搜索结果
			ScanRecordCompat scanRecordCompat = ((BluetoothLeScanResultEvent) commonResponseEvent).getScanRecord();
			List<ParcelUuid> serviceList = scanRecordCompat.getServiceUuids();
			if (serviceList != null && serviceList.contains(new ParcelUuid(Constants.CSR_MESH_SERVICE))) {
				String macAddress = ((BluetoothLeScanResultEvent) commonResponseEvent).getMacAddress();
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
					Toast.makeText(MainActivity.this, "scan timeout", Toast.LENGTH_SHORT).show();
					break;
				case GATT_CONNECTED:
					communicationService.readCharacteristic("00:02:5B:00:25:13");
					break;
				case GATT_CONNECT_TIMEOUT:
				case GATT_DISCONNECTED:
				case GATT_CONNECTION_ERROR:
				case GATT_REMOTE_DISAPPEARED:
					Toast.makeText(MainActivity.this, "connection error", Toast.LENGTH_SHORT).show();
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

	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing()) {
			App.getInstance().exitApp();
		}
	}
}
