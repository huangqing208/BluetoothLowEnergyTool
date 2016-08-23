package cn.bit.hao.ble.tool.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.bit.hao.ble.tool.R;
import cn.bit.hao.ble.tool.adapter.ScanResultAdapter;
import cn.bit.hao.ble.tool.bluetooth.scan.BluetoothLeScanManager;
import cn.bit.hao.ble.tool.response.events.CommonResponseEvent;
import cn.bit.hao.ble.tool.response.events.bluetooth.BluetoothLeScanResultEvent;

public class ScanLeDevicesActivity extends BleCommunicationActivity {
	private static final String TAG = ScanLeDevicesActivity.class.getSimpleName();

	private RecyclerView scanResults;
	private ScanResultAdapter scanResultAdapter;
	private RecyclerView.LayoutManager layoutManager;
	private List<ScanResultAdapter.ScanResult> scanResultList;

	private Map<String, Runnable> timeoutMap;
	private static final int SCAN_RESULT_TIMEOUT = 15000;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_le_devices);

		mHandler = new Handler();

		findView();
		setListener();
	}

	private void findView() {
		scanResults = (RecyclerView) findViewById(R.id.scan_results);

		layoutManager = new LinearLayoutManager(this);
		scanResults.setLayoutManager(layoutManager);

		scanResultList = new ArrayList<>();
		timeoutMap = new HashMap<>();
		scanResultAdapter = new ScanResultAdapter(scanResultList);
		scanResults.setAdapter(scanResultAdapter);
	}

	private void setListener() {
		scanResultAdapter.setOnItemClickListener(new ScanResultAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(ScanResultAdapter.ViewHolder viewHolder, int position) {

			}
		});
	}

	@Override
	protected void onCommunicationServiceBound() {
//		communicationService.startLeScan();
	}

	@Override
	protected void onStart() {
		super.onStart();
		BluetoothLeScanManager.getInstance().startLeScan(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		BluetoothLeScanManager.getInstance().stopLeScan(this);
		for (String key : timeoutMap.keySet()) {
			mHandler.removeCallbacks(timeoutMap.get(key));
		}
		timeoutMap.clear();
	}

	@Override
	protected void beforeCommunicationServiceUnbound() {
//		communicationService.stopLeScan();
	}

	@Override
	public void onCommonResponded(CommonResponseEvent commonResponseEvent) {
		if (commonResponseEvent instanceof BluetoothLeScanResultEvent) {
			BluetoothLeScanResultEvent scanResultEvent = (BluetoothLeScanResultEvent) commonResponseEvent;
			if (scanResultEvent.getDevice().getName() == null
					|| scanResultEvent.getDevice().getName().length() == 0
					|| scanResultEvent.getScanRecord().getServiceUuids() == null
					|| scanResultEvent.getScanRecord().getServiceUuids().size() == 0) {
				return;
			}
			Log.i(TAG, scanResultEvent.toString());
			boolean existing = false;
			for (int i = 0; i < scanResultList.size(); ++i) {
				ScanResultAdapter.ScanResult scanResult = scanResultList.get(i);
				if (scanResult.device.getAddress().equals(scanResultEvent.getDevice().getAddress())) {
					scanResult.device = scanResultEvent.getDevice();
					scanResult.rssi = scanResultEvent.getRssi();
					scanResult.scanRecord = scanResultEvent.getScanRecord();
					scanResultAdapter.notifyItemChanged(i);
					existing = true;
					break;
				}
			}
			if (!existing) {
				scanResultList.add(new ScanResultAdapter.ScanResult(scanResultEvent.getDevice(),
						scanResultEvent.getRssi(), scanResultEvent.getScanRecord()));
				scanResultAdapter.notifyItemInserted(scanResultList.size() - 1);
			}
			resetTimeoutTask(scanResultEvent.getDevice().getAddress());
		}
	}

	private void resetTimeoutTask(final String macAddress) {
		Runnable task = timeoutMap.get(macAddress);
		if (task != null) {
			mHandler.removeCallbacks(task);
		} else {
			task = new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < scanResultList.size(); ++i) {
						if (scanResultList.get(i).device.getAddress().equals(macAddress)) {
							scanResultList.remove(i);
							scanResultAdapter.notifyItemRemoved(i);
							break;
						}
					}
					timeoutMap.remove(macAddress);
				}
			};
			timeoutMap.put(macAddress, task);
		}
		mHandler.postDelayed(task, SCAN_RESULT_TIMEOUT);
	}

}
