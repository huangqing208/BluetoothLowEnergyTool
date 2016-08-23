/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.adapter;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.bit.hao.ble.tool.R;
import cn.bit.hao.ble.tool.bluetooth.utils.ScanRecordCompat;

/**
 * @author wuhao on 2016/8/22
 */
public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ViewHolder> {
	private static final String TAG = ScanResultAdapter.class.getSimpleName();

	public static class ViewHolder extends RecyclerView.ViewHolder {

		public TextView deviceName;
		public TextView macAddress;
		public TextView rssi;
		public TextView service;
		public TextView otherInfo;
		public View itemView;

		public ViewHolder(View itemView) {
			super(itemView);
			this.itemView = itemView;
			this.deviceName = (TextView) itemView.findViewById(R.id.device_name);
			this.macAddress = (TextView) itemView.findViewById(R.id.mac_address);
			this.service = (TextView) itemView.findViewById(R.id.service);
			this.rssi = (TextView) itemView.findViewById(R.id.rssi);
			this.otherInfo = (TextView) itemView.findViewById(R.id.other_info);
		}
	}

	private List<ScanResult> scanResults;

	public ScanResultAdapter(List<ScanResult> scanResults) {
		this.scanResults = scanResults;
	}

	public interface OnItemClickListener {
		void onItemClick(ViewHolder viewHolder, int position);
	}

	private OnItemClickListener listener;

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detected_devices_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		ScanResult scanResult = scanResults.get(position);
		holder.deviceName.setText(scanResult.device.getName());
		holder.macAddress.setText(scanResult.device.getAddress());
		holder.rssi.setText(scanResult.rssi + "dBm");
		if (scanResult.scanRecord.getServiceUuids() != null
				&& scanResult.scanRecord.getServiceUuids().size() > 0) {
			holder.service.setText(scanResult.scanRecord.getServiceUuids().get(0).toString());
		}
		holder.otherInfo.setText(scanResult.scanRecord.toString());
	}

	@Override
	public int getItemCount() {
		return scanResults == null ? 0 : scanResults.size();
	}

	public static class ScanResult {
		public BluetoothDevice device;
		public int rssi;
		public ScanRecordCompat scanRecord;

		public ScanResult(BluetoothDevice device, int rssi, ScanRecordCompat scanRecord) {
			this.device = device;
			this.rssi = rssi;
			this.scanRecord = scanRecord;
		}
	}

}
