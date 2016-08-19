/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.bit.hao.ble.tool.R;

/**
 * @author wuhao on 2016/8/19
 */
public class FunctionListAdapter extends RecyclerView.Adapter<FunctionListAdapter.ViewHolder> {
	private static final String TAG = FunctionListAdapter.class.getSimpleName();

	public static class ViewHolder extends RecyclerView.ViewHolder {

		public ImageView icon;
		public TextView text;
		public View view;

		public ViewHolder(View view) {
			super(view);
			this.view = view;
			this.icon = (ImageView) view.findViewById(R.id.item_icon);
			this.text = (TextView) view.findViewById(R.id.item_text);
		}
	}

	private Context context;
	private int[] mTexts;
	private int[] mIcons;

	public FunctionListAdapter(Context context, int[] texts, int[] icons) {
		this.context = context;
		this.mTexts = texts;
		this.mIcons = icons;
	}

	public interface OnItemClickListener {
		public void onItemClick(View view, int position);
	}

	private OnItemClickListener listener;

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}

	@Override
	public int getItemViewType(int position) {
		Log.i(TAG, "getItemViewType " + position);
		return super.getItemViewType(position);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		Log.i(TAG, "onCreateViewHolder: " + viewType);
		int layoutId = R.layout.function_list_item;
		View v = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
		// 两种布局的子view类型及对应ID均相同，所以可以共用ViewHolder
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, final int position) {
		holder.icon.setImageResource(mIcons[position]);
		holder.text.setText(context.getString(mTexts[position]));
		holder.view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onItemClick(v, position);
				}
			}
		});
	}

	@Override
	public int getItemCount() {
		return mTexts != null ? mTexts.length : 0;
	}
}
