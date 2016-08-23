package cn.bit.hao.ble.tool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.bit.hao.ble.tool.R;
import cn.bit.hao.ble.tool.adapter.FunctionListAdapter;
import cn.bit.hao.ble.tool.application.App;

public class FunctionListActivity extends BaseActivity {

	private CoordinatorLayout coordinatorLayout;
	private RecyclerView functionList;
	private FunctionListAdapter adapter;
	private RecyclerView.LayoutManager layoutManager;

	private List<Integer> functionTexts;
	private List<Integer> functionIcons;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_function_list);

		findView();

		setListener();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing()) {
			App.getInstance().exitApp();
		}
	}

	private void findView() {
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

		functionList = (RecyclerView) findViewById(R.id.function_list);
		functionList.setHasFixedSize(true);

//		layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
		layoutManager = new LinearLayoutManager(this);
		functionList.setLayoutManager(layoutManager);

		functionTexts = new ArrayList<>();
		functionTexts.addAll(Arrays.asList(
				R.string.scan_le_device,
				R.string.connect_le_device));
		functionIcons = new ArrayList<>();
		functionIcons.addAll(Arrays.asList(
				R.mipmap.ic_bluetooth_searching_black_36dp,
				R.mipmap.ic_bluetooth_black_36dp));
		adapter = new FunctionListAdapter(this, functionTexts, functionIcons);
		functionList.setAdapter(adapter);
	}

	private void setListener() {
		adapter.setOnItemClickListener(new FunctionListAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(FunctionListAdapter.ViewHolder viewHolder, int position) {
				switch (position) {
					case 0:
						startActivity(new Intent(FunctionListActivity.this, ScanLeDevicesActivity.class));
						break;
					case 1:
						break;
					default:
						break;
				}
//				Snackbar.make(coordinatorLayout, "position: " + position, Snackbar.LENGTH_SHORT)
//						.setAction("OK", new View.OnClickListener() {
//							@Override
//							public void onClick(View v) {
//								startActivity(new Intent(FunctionListActivity.this, MainActivity.class));
//							}
//						}).show();
			}
		});


	}

}
