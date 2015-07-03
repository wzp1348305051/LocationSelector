package com.wzp.locationselector.activity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.wzp.locationselector.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends Activity implements OnClickListener {
	private DetailActivity instance = this;
	private ImageView ivBack;// 返回键
	private MapView mvPreview;
	private ImageView ivLocate;// 定位键
	private BaiduMap map;
	private double lat;
	private double lng;
	private int logNum = 0;
	private Marker markerCurrent = null;// 定位的Marker
	private BitmapDescriptor bitmapCurrent = BitmapDescriptorFactory.fromResource(R.drawable.ic_dot_current);// 当前定位的位置
	private BitmapDescriptor bitmapSelected = BitmapDescriptorFactory.fromResource(R.drawable.ic_dot_selected);// 当前选中的位置
	private LocationClient locationClient = null;// 定位模块
	private int LocateNum = 0;// 定位次数，因为地图初始化时定位到用户分享的地点，所以得忽略前两次定位，至于为什么是前两次定位，是因为调用locationClient.start();时百度地图会定位两次，以后调用locationClient.requestLocation();只会定位一次
	private String address;
	private BDLocationListener locationListener = new MyLocationListener();
	private class MyLocationListener implements BDLocationListener {// 定位模块监听接口
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null || mvPreview == null)
				return;
			Log.d("num", String.valueOf(++logNum));
			if (++LocateNum > 2) {
				LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
				if (markerCurrent != null) {
					markerCurrent.remove();// 删除旧图标
					markerCurrent = null;
				}
				markerCurrent = addMarker(latLng, bitmapCurrent);// 添加定位的Marker图标
				moveToCenter(latLng);// 将当前位置移至屏幕中央
			}
		}
	}
	
	/**
	 * 启动活动的入口方法
	 * params
	 * context:活动调用的上下文环境
	 * lat:经度
	 * lng:纬度
	 * */
	public static void actionStart(Context context, double lat, double lng, String address) {
		Intent intent = new Intent(context, DetailActivity.class);
		intent.putExtra("lat", lat);
		intent.putExtra("lng", lng);
		intent.putExtra("address", address);
		context.startActivity(intent);
	}
	
	@Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        
        initData();
        initLayout();
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
	
	private void initData() {
		Intent intent = getIntent();
		lat = intent.getDoubleExtra("lat", 32.048177);
		lng = intent.getDoubleExtra("lng", 118.79065);
		address = intent.getStringExtra("address");
	}
	
	/**
	 * 初始化布局
	 * */
	private void initLayout() {
		setContentView(R.layout.activity_detail);
		
		ivBack = (ImageView) findViewById(R.id.iv_detail_back);
		ivLocate = (ImageView) findViewById(R.id.iv_detail_locate);
		
		initEvent();
		initLocation();// 初始化定位模块
		initBaiduMap();// 初始化地图模块
	}
	
	/**
	 * 初始化事件
	 * */
	private void initEvent() {
		ivBack.setOnClickListener(instance);
		ivLocate.setOnClickListener(instance);
	}
	
	/**
	 * 初始化地图模块
	 * */
	private void initBaiduMap() {
		mvPreview = (MapView) findViewById(R.id.mv_detail_preview);
		mvPreview.showScaleControl(false);// 设置是否显示比例尺控件
		mvPreview.showZoomControls(false);// 设置是否显示缩放控件
		map = mvPreview.getMap();
		map.setMapType(BaiduMap.MAP_TYPE_NORMAL);// 普通地图
		map.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(16).target(new LatLng(lat, lng)).build()));//设置默认缩放等级和默认中心点
		LatLng latLng = new LatLng(lat, lng);
		addMarker(latLng, bitmapSelected);
		/**添加弹窗覆盖物*/
		TextView tvAddress = new TextView(instance);
		tvAddress.setText(address);
		InfoWindow mInfoWindow = new InfoWindow(tvAddress, latLng, -108); 
		map.showInfoWindow(mInfoWindow);
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
	 * 将指定经纬度地标移至屏幕中心点
	 * */
	private void moveToCenter(LatLng latLng) {
		map.animateMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
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
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_detail_back:
			onBackPressed();
			break;
		case R.id.iv_detail_locate:
			Toast.makeText(instance, "正在定位...", Toast.LENGTH_SHORT).show();
			locationClient.requestLocation();
			break;
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
    }
	
}
