package com.wzp.locationselector.adapter;

import java.util.List;

import com.wzp.locationselector.R;
import com.wzp.locationselector.bean.SuggestionLocation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LocationActivityListAdapter extends BaseAdapter {
	private List<SuggestionLocation> data;
	private LayoutInflater inflater;
	private class ViewHolder {
		ImageView ivSelected;
		TextView tvName;
		TextView tvAddress;
	}
	
	public LocationActivityListAdapter(Context context, List<SuggestionLocation> data) {
		this.data = data;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return (null == data) ? 0 : data.size();
	}

	@Override
	public SuggestionLocation getItem(int position) {
		if (data.get(position) != null) {
			return data.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view  = null;
		ViewHolder holder = null;
		if (convertView == null) {
			view = inflater.inflate(R.layout.activity_location_list_item, null);
			holder = new ViewHolder();
			holder.ivSelected = (ImageView) view.findViewById(R.id.iv_location_list_item_select);
			holder.tvName = (TextView) view.findViewById(R.id.tv_location_list_item_name);
			holder.tvAddress = (TextView) view.findViewById(R.id.tv_location_list_item_address);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		SuggestionLocation suggestion = getItem(position);
		
		if (suggestion != null) {
			if (suggestion.getSelected()) {//设置选中按钮是否显示
				holder.ivSelected.setImageResource(R.drawable.ic_item_selected);
			} else {
				holder.ivSelected.setImageBitmap(null);
			}
			holder.tvName.setText(suggestion.getName());
			holder.tvAddress.setText(suggestion.getAddress());
		}
		
		return view;
	}
	
}
