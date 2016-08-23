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

import java.util.List;

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
	private List<Integer> mTexts;
	private List<Integer> mIcons;

	public FunctionListAdapter(Context context, List<Integer> texts, List<Integer> icons) {
		this.context = context;
		this.mTexts = texts;
		this.mIcons = icons;
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
		Log.i(TAG, "onCreateViewHolder: " + viewType);
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.function_list_item, parent, false);
		// 两种布局的子view类型及对应ID均相同，所以可以共用ViewHolder
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final ViewHolder holder, int position) {
		Log.i(TAG, "onBindViewHolder: " + position);
		holder.icon.setImageResource(mIcons.get(position));
		holder.text.setText(context.getString(mTexts.get(position)));
		holder.view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int adapterPosition = holder.getAdapterPosition();
				if (listener != null && adapterPosition != RecyclerView.NO_POSITION) {
					listener.onItemClick(holder, adapterPosition);
				}
			}
		});
	}

	@Override
	public int getItemCount() {
		Log.i(TAG, "getItemCount");
		return mTexts != null ? mTexts.size() : 0;
	}
}
