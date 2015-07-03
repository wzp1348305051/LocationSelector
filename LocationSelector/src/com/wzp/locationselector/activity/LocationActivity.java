package com.wzp.locationselector.activity;

import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.wzp.locationselector.R;
import com.wzp.locationselector.adapter.LocationActivityListAdapter;
import com.wzp.locationselector.bean.SuggestionLocation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LocationActivity extends Activity implements OnClickListener {
	private final int REQUEST_SEARCH = 101;// 搜索按钮的请求源
	private LocationActivity instance = this;
	private ImageView ivBack;// 返回键
	private ImageView ivSearch;// 搜索键
	private TextView tvOK;// 确认键
	private ImageView ivLocate;// 定位键
	private ListView lvSuggestion;
	private ProgressBar pbLoading;
	private LocationActivityListAdapter adapter;
	private List<SuggestionLocation> suggestions = new ArrayList<SuggestionLocation>();// 包括当前定位在内的所有建议地点信息
	private boolean mapStateChangedByDrag = true;// 是否由于地图拖拽引发的地图状态的改变的开关变量，拖拽时此值为true，listItem选中时此值为false，为false不会触发刷新列表数据
	private MapView mvPreview;
	private BaiduMap map;
	private BitmapDescriptor bitmapCurrent = BitmapDescriptorFactory.fromResource(R.drawable.ic_dot_current);// 当前定位的位置
	private BitmapDescriptor bitmapSelected = BitmapDescriptorFactory.fromResource(R.drawable.ic_dot_selected);// 当前选中的位置
	private Marker markerCurrent = null;// 定位的Marker
	private Marker markerSelected = null;// 选中的Marker
	private String city = null;// 当前定位所在城市，用于搜索功能
	private SuggestionLocation locationSelected = null;// 选中的SuggestionLocation
	private GeoCoder geoSearch = null;// Geo搜索模块
	private LocationClient locationClient = null;// 定位模块
	private BDLocationListener locationListener = new MyLocationListener();
	private class MyLocationListener implements BDLocationListener {// 定位模块监听接口
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null || mvPreview == null)
				return;
			city = location.getCity();
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			if (markerSelected != null) {
				markerSelected.remove();
				markerSelected = null;
			}
			if (markerCurrent != null) {
				markerCurrent.remove();// 删除旧图标
				markerCurrent = null;
			}
			markerCurrent = addMarker(latLng, bitmapCurrent);// 添加定位的Marker图标
			moveToCenter(latLng);// 将当前位置移至屏幕中央
			getSuggestionLocations(latLng);// 获取建议列表
		}
	}
	
	/**
	 * 启动活动的入口方法
	 * params
	 * context:活动调用的上下文环境
	 * REQUEST:startActivityForResult的请求码
	 * */
	public static void actionStart(Context context, final int REQUEST) {
		Intent intent = new Intent(context, LocationActivity.class);
		((Activity)context).startActivityForResult(intent, REQUEST);
	}
	
	@Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        
        initLayout();
    }
	
	/**
	 * 初始化布局
	 * */
	private void initLayout() {
		setContentView(R.layout.activity_location);
		
		ivBack = (ImageView) findViewById(R.id.iv_location_back);
		ivSearch = (ImageView) findViewById(R.id.iv_location_search);
		tvOK = (TextView) findViewById(R.id.tv_location_ok);
		ivLocate = (ImageView) findViewById(R.id.iv_location_locate);
		lvSuggestion = (ListView) findViewById(R.id.lv_location_suggestion);
		pbLoading = (ProgressBar) findViewById(R.id.pb_location_loading);
		
		adapter = new LocationActivityListAdapter(instance, suggestions);
		lvSuggestion.setAdapter(adapter);
		
		initEvent();
		initBaiduMap();// 初始化地图模块
		initLocation();// 初始化定位模块
		initGeoSearch();// 初始化Geo搜索模块
		
	}
	
	/**
	 * 初始化地图模块
	 * */
	private void initBaiduMap() {
		mvPreview = (MapView) findViewById(R.id.mv_location_preview);
		mvPreview.showScaleControl(false);// 设置是否显示比例尺控件
		mvPreview.showZoomControls(false);// 设置是否显示缩放控件
		map = mvPreview.getMap();
		map.setMapType(BaiduMap.MAP_TYPE_NORMAL);// 普通地图
		map.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(16).target(new LatLng(32.048177, 118.79065)).build()));//设置默认缩放等级和默认中心点
		map.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {

			@Override
			public void onMapStatusChange(MapStatus arg0) {
				
			}

			@Override
			public void onMapStatusChangeFinish(MapStatus arg0) {
				if (mapStateChangedByDrag) {
					getSuggestionLocations(map.getMapStatus().target);// 获取建议列表
				}
				mapStateChangedByDrag = true;
			}

			@Override
			public void onMapStatusChangeStart(MapStatus arg0) {
				
			}
			
		});
	}
	
	/**
	 * 初始化定位模块
	 * */
	private void initLocation() {
		locationClient = new LocationClient(instance);
		locationClient.registerLocationListener(locationListener);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);//设置定位模式
		option.setCoorType("bd09ll");//返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(100);//设置发起定位请求的间隔时间为100ms,即只定位一次
		option.setIsNeedAddress(true);//返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);//返回的定位结果包含手机机头的方向
		locationClient.setLocOption(option);
		locationClient.start();
	}
	
	/**
	 * 初始化Geo搜索模块
	 * */
	private void initGeoSearch() {
		geoSearch = GeoCoder.newInstance();
		geoSearch.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
			
			/**
		     * 地理编码，由地点名称获取经纬度
		     * */
			@Override
			public void onGetGeoCodeResult(GeoCodeResult result) {
				
			}
			
			/**
			 * 反地理编码，由地点经纬度获取地点名称
			 * */
			@Override
			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
				refreshListView(result);
			}
			
		});
	}
	
	/**
	 * 初始化事件
	 * */
	private void initEvent() {
		ivBack.setOnClickListener(instance);
		ivSearch.setOnClickListener(instance);
		tvOK.setOnClickListener(instance);
		ivLocate.setOnClickListener(instance);
		lvSuggestion.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				locationSelected.SetSelected(false);// 清除旧的选中图标
				SuggestionLocation suggestion = suggestions.get(position);
				suggestion.SetSelected(true);
				LatLng location = suggestion.getLocation();
				if (markerSelected != null) {
					markerSelected.remove();// 删除旧图标
					markerSelected = null;
				}
				markerSelected = addMarker(location, bitmapSelected);// 添加选中的Marker图标
				
				moveToCenter(location);// 将当前位置移至屏幕中央
				locationSelected = suggestion;
				adapter.notifyDataSetChanged();//刷新界面，此处未做列表的局部刷新，有待改进
			}
			
		});
	}
	
    @Override  
    protected void onDestroy() {          
        locationClient.stop();// 销毁定位
        locationClient.unRegisterLocationListener(locationListener);
        mvPreview.onDestroy();
        mvPreview = null;
        
        super.onDestroy();
    }
    
    @Override  
    protected void onResume() {
    	mvPreview.onResume();
        super.onResume();
    }
    
    @Override  
    protected void onPause() {
    	mvPreview.onPause();
        super.onPause();
    }
	
	/**
	 * 在地图指定的位置上添加标注
	 * */
	private Marker addMarker(LatLng latLng, BitmapDescriptor icon) {
		//构建MarkerOption，用于在地图上添加Marker  
		OverlayOptions option = new MarkerOptions().position(latLng).icon(icon);  
		//在地图上添加Marker，并显示  
		return (Marker) map.addOverlay(option);
	}	
	
	/**
	 * 将指定经纬度地标移至屏幕中心点
	 * */
	private void moveToCenter(LatLng latLng) {
		mapStateChangedByDrag = false;
		map.animateMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
	}
	
	/**
	 * 获取附近的建议地点
	 * */
	private void getSuggestionLocations(LatLng latLng) {
		pbLoading.setVisibility(View.VISIBLE);
		ReverseGeoCodeOption option = new ReverseGeoCodeOption();// 根据当前位置进行Geo搜索，获取附近建议地点信息
		option.location(latLng);
		geoSearch.reverseGeoCode(option);
	}
	
	/**
	 * 刷新列表
	 * */
	private void refreshListView(ReverseGeoCodeResult result) {
		suggestions.clear();// 清除旧的结果集
		if (markerSelected != null) {// 清除旧的选中图标
			markerSelected.remove();
			markerSelected = null;
		}
		locationSelected = null;
		SuggestionLocation suggestion = new SuggestionLocation();
		suggestion.SetSelected(true);
		suggestion.SetName("[位置]");
		suggestion.SetAddress(result.getAddress());
		suggestion.SetLocation(result.getLocation());
		suggestions.add(suggestion);// 将当前位置作为列表首项
		locationSelected = suggestion;// 设置列表首选项为当前选中的项
		markerSelected = addMarker(result.getLocation(), bitmapSelected);// 添加选中的Marker图标
		List<PoiInfo> infos = result.getPoiList();// 获取其余Poi建议
		for (PoiInfo info : infos) {
			SuggestionLocation location = new SuggestionLocation();
			location.SetSelected(false);
			location.SetName(info.name);
			location.SetAddress(info.address);
			location.SetLocation(info.location);
			suggestions.add(location);
		}
		adapter.notifyDataSetChanged();
		pbLoading.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_location_back:
			onBackPressed();
			break;
		case R.id.iv_location_search:
			if (city == null) {
				Toast.makeText(instance, "定位中，搜索功能暂不可用！", Toast.LENGTH_SHORT).show();
			} else {
				SearchActivity.actionStart(instance, REQUEST_SEARCH, city);
			}
			break;
		case R.id.tv_location_ok:
			if (locationSelected == null) {
				Toast.makeText(instance, "定位中，无法选择地点！", Toast.LENGTH_SHORT).show();
			} else {
				LatLng latLng = locationSelected.getLocation();
				Intent intent = new Intent();
				intent.putExtra("lat", latLng.latitude);
				intent.putExtra("lng", latLng.longitude);
				intent.putExtra("address", locationSelected.getAddress());
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
//			map.snapshot(new SnapshotReadyCallback() {
//
//				@Override
//				public void onSnapshotReady(Bitmap bitmap) {
//					saveBitmap(bitmap);
//				}
//				
//			});
			break;
		case R.id.iv_location_locate:
			Toast.makeText(instance, "正在定位...", Toast.LENGTH_SHORT).show();
			locationClient.requestLocation();
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case Activity.RESULT_OK:
			switch (requestCode) {
			case REQUEST_SEARCH:
				LatLng latLng = new LatLng(data.getDoubleExtra("lat", 0), data.getDoubleExtra("lng", 0));
				moveToCenter(latLng);// 将当前位置移至屏幕中央
				getSuggestionLocations(latLng);// 获取建议列表
				break;
			}
			break;
		case Activity.RESULT_CANCELED:
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		setResult(Activity.RESULT_CANCELED, intent);
		super.onBackPressed();
    }
	
//	/**保存地图截屏*/
//	private void saveBitmap(Bitmap bitmap) {
//		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
//		String fileName = format.format(new Date());// 根据当前时间构造唯一的文件名
//		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + fileName + ".jpg");
//		try {
//			FileOutputStream out = new FileOutputStream(file);
//			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
//		    out.flush();
//		    out.close();
//		    Toast.makeText(instance, "截图保存成功！", Toast.LENGTH_SHORT).show();
//		} catch (FileNotFoundException e) {
//			Toast.makeText(instance, "截图保存失败！", Toast.LENGTH_SHORT).show();
//		} catch (IOException e) {
//			Toast.makeText(instance, "截图保存失败！", Toast.LENGTH_SHORT).show();
//		}
//	}

	
}
