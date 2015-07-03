package com.wzp.locationselector.adapter;

import java.util.List;

import com.wzp.locationselector.R;
import com.wzp.locationselector.bean.SearchResult;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SearchActivityListAdapter extends BaseAdapter {
	private List<SearchResult> data;
	private LayoutInflater inflater;
	private class ViewHolder {
		TextView tvName;
		TextView tvAddress;
	}
	
	public SearchActivityListAdapter(Context context, List<SearchResult> data) {
		this.data = data;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return (null == data) ? 0 : data.size();
	}

	@Override
	public SearchResult getItem(int position) {
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
			view = inflater.inflate(R.layout.activity_search_list_item, null);
			holder = new ViewHolder();
			holder.tvName = (TextView) view.findViewById(R.id.tv_search_list_item_name);
			holder.tvAddress = (TextView) view.findViewById(R.id.tv_search_list_item_address);
			view.setTag(holder);
		} else {
			view = convertView;
			holder = (ViewHolder) view.getTag();
		}
		SearchResult result = getItem(position);
		
		if (result != null) {
			holder.tvName.setText(result.getName());
			holder.tvAddress.setText(result.getAddress());
		}
		
		return view;
	}
}
