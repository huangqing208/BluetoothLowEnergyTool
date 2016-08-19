package cn.bit.hao.ble.tool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import cn.bit.hao.ble.tool.R;
import cn.bit.hao.ble.tool.adapter.FunctionListAdapter;

public class FunctionListActivity extends BaseActivity {

	private CoordinatorLayout coordinatorLayout;
	private RecyclerView functionList;
	private FunctionListAdapter adapter;
	private RecyclerView.LayoutManager layoutManager;

	private static final int[] functionTexts = {R.string.scan_le_device, R.string.connect_le_device};
	private static final int[] functionIcons = {R.mipmap.ic_bluetooth_searching_black_36dp,
			R.mipmap.ic_bluetooth_black_36dp};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_function_list);

		findView();

		setListener();
	}

	private void findView() {
		coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

		functionList = (RecyclerView) findViewById(R.id.function_list);
		functionList.setHasFixedSize(true);

//		layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
		layoutManager = new LinearLayoutManager(this);
		functionList.setLayoutManager(layoutManager);

		adapter = new FunctionListAdapter(this, functionTexts, functionIcons);
		functionList.setAdapter(adapter);
	}

	private void setListener() {
		adapter.setOnItemClickListener(new FunctionListAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position) {
				Snackbar.make(coordinatorLayout, "position: " + position, Snackbar.LENGTH_SHORT)
						.setAction("OK", new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								startActivity(new Intent(FunctionListActivity.this, MainActivity.class));
							}
						}).show();
			}
		});
	}

}
