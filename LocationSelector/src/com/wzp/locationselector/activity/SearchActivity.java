package com.wzp.locationselector.activity;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.wzp.locationselector.R;
import com.wzp.locationselector.adapter.SearchActivityListAdapter;
import com.wzp.locationselector.bean.SearchResult;
import com.wzp.locationselector.view.XListView;
import com.wzp.locationselector.view.XListView.IXListViewListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class SearchActivity extends Activity implements OnClickListener, IXListViewListener {
	private SearchActivity instance = this;
	private ImageView ivBack;
	private EditText etInput;
	private TextView tvOK;
	private XListView lvContent;
	private ProgressBar pbLoading;
	private SearchActivityListAdapter adapter;
	private List<SearchResult> data = new ArrayList<SearchResult>();
	private PoiSearch poiSearch;// 百度poi搜索
	private String city;// poi搜索需要用到的参数，由调用活动通过定位获得并传递过来
	private final int pageCapacity = 10;// 设置每页容量，默认为每页10条
	private int pageNum = 0;// 分页编号
	private boolean hasMore = true;// 是否还有更多的分页数据
	
	/**
	 * 启动活动的入口方法
	 * params
	 * context:活动调用的上下文环境
	 * REQUEST:startActivityForResult的请求码
	 * city:poi搜索需要用到的参数，由调用活动通过定位获得并传递过来
	 * */
	public static void actionStart(Context context, final int REQUEST, String city) {
		Intent intent = new Intent(context, SearchActivity.class);
		intent.putExtra("city", city);
		((Activity)context).startActivityForResult(intent, REQUEST);
	}
	
	@Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        
        initData();
        initLayout();
    }
	
	private void initData() {
		Intent intent = getIntent();
		city = intent.getStringExtra("city");
	}
	
	/**
	 * 初始化布局
	 * */
	private void initLayout() {
		setContentView(R.layout.activity_search);
		
		ivBack = (ImageView) findViewById(R.id.iv_search_back);
		etInput = (EditText) findViewById(R.id.et_search_input);
		tvOK = (TextView) findViewById(R.id.tv_search_ok);
		lvContent = (XListView) findViewById(R.id.lv_search_content);
		pbLoading = (ProgressBar) findViewById(R.id.pb_search_loading);
		
		initEvent();
		initPoiSearch();
	}
	
	/**
	 * 初始化事件
	 * */
	private void initEvent() {
		ivBack.setOnClickListener(instance);
		tvOK.setOnClickListener(instance);
		etInput.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable e) {
				if (e.length() != 0) {
					tvOK.setClickable(true);
				} else {
					tvOK.setClickable(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence c, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence c, int start, int before, int count) {
				
			}
			
		});
		lvContent.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				SearchResult result = data.get(position);
				Intent intent = new Intent();
				intent.putExtra("lat", result.getLocation().latitude);
				intent.putExtra("lng", result.getLocation().longitude);
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
			
		});
		
	}
	
	/**
	 * 初始化百度poi搜索
	 * */
	private void initPoiSearch() {
		poiSearch = PoiSearch.newInstance();
		poiSearch.setOnGetPoiSearchResultListener(new OnGetPoiSearchResultListener() {

			@Override
			public void onGetPoiDetailResult(PoiDetailResult result) {
				
			}

			@Override
			public void onGetPoiResult(PoiResult result) {
				if (result != null) {
					List<PoiInfo> infos = result.getAllPoi();
					if (infos.size() == 0) {
						Toast.makeText(instance, "未找到搜索结果！", Toast.LENGTH_SHORT).show();
					} else {
						for (PoiInfo info : infos) {
							SearchResult temp = new SearchResult();
							temp.SetName(info.name);
							temp.SetAddress(info.address);
							temp.SetLocation(info.location);
							data.add(temp);
						}
						if (adapter == null) {
							lvContent.removeHeaderView();// 原先列表带下拉刷新上拉加载功能，现移除下拉刷新功能，故移除header
							lvContent.setXListViewListener(instance);
							adapter = new SearchActivityListAdapter(instance, data);
							lvContent.setAdapter(adapter);
						} else {
							adapter.notifyDataSetChanged();
						}
					}
					if (infos.size() < 10) {
						hasMore = false;
					}
				} else {
					hasMore = false;
				}
				lvContent.onLoadMoreComplete();
				lvContent.setPullLoadEnable(hasMore);//设置XListView的footer是否显示，是否可上拉加载
				pbLoading.setVisibility(View.INVISIBLE);
			}
			
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_search_back:
			onBackPressed();
			break;
		case R.id.tv_search_ok:
			data.clear();
			pageNum = 0;
			hasMore = true;
			search();
			break;
		}
	}
	
	/**
	 * 根据关键字进行poi搜索
	 * */
	private void search() {
		pbLoading.setVisibility(View.VISIBLE);
		poiSearch.searchInCity((new PoiCitySearchOption()).city(city).keyword(etInput.getText().toString()).pageCapacity(pageCapacity).pageNum(pageNum));
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		setResult(Activity.RESULT_CANCELED, intent);
		super.onBackPressed();
    }

	@Override
	public void onRefresh() {
		// 此活动中禁用下拉刷新功能
	}

	@Override
	public void onLoadMore() {
		if (hasMore) {
			pageNum++;
			search();
		} else {
			Toast.makeText(instance, "已无更多数据！", Toast.LENGTH_SHORT).show();
		}
	}
	
}
