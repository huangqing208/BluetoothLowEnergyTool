package cn.bit.hao.ble.tool.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.bit.hao.ble.tool.R;
import cn.bit.hao.ble.tool.adapter.FunctionListAdapter;

public class FunctionListActivity extends BaseActivity {

	private CoordinatorLayout coordinatorLayout;
	private RecyclerView functionList;
	private FunctionListAdapter adapter;
	private RecyclerView.LayoutManager layoutManager;

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

		layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
		functionList.setLayoutManager(layoutManager);

		List<String> texts = new ArrayList<>();
		List<Integer> icons = new ArrayList<>();
		for (int i = 0; i < 21; ++i) {
			String string = "";
			for (int l = 0; l <= i; ++l) {
				string += "" + l;
			}
			texts.add(string);
			icons.add(R.mipmap.ic_launcher);
		}

		adapter = new FunctionListAdapter(texts, icons);
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
								finish();
							}
						}).show();
			}
		});
	}

}
