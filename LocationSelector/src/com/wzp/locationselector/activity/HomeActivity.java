package com.wzp.locationselector.activity;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.wzp.locationselector.R;
import com.wzp.locationselector.util.ImageOption;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeActivity extends Activity implements OnClickListener {
	private final int REQUEST_LOCATION = 100;
	private HomeActivity instance = this;
	private TextView tvOK;
	private TextView tvAddress;
	private ImageView ivPreview;
	private String address = null;// 用户返回的位置的地址
	private double lat = 0;
	private double lng = 0;
	
	@Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);
        
        initLayout();
    }
	
	/**
	 * 初始化布局
	 * */
	private void initLayout() {
		setContentView(R.layout.activity_home);
		
		tvOK = (TextView) findViewById(R.id.tv_home_ok);
		tvAddress = (TextView) findViewById(R.id.tv_home_address);
		ivPreview = (ImageView) findViewById(R.id.iv_home_preview);
		
		initEvent();
	}

	private void initEvent() {
		tvOK.setOnClickListener(instance);
		ivPreview.setOnClickListener(instance);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_home_ok:
			LocationActivity.actionStart(instance, REQUEST_LOCATION);
			break;
		case R.id.iv_home_preview:
			if (address != null) {
				DetailActivity.actionStart(instance, lat, lng, address);
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) {
		case Activity.RESULT_OK:
			switch (requestCode) {
			case REQUEST_LOCATION:
				address = data.getStringExtra("address");
				tvAddress.setText(address);
				lat = data.getDoubleExtra("lat", 0);
				lng = data.getDoubleExtra("lng", 0);
				String url = "http://api.map.baidu.com/staticimage?width=640&height=480&center=" + lng + "," + lat + "&zoom=15";// 此处需要特别注意，拼接url的时候纬度在前经度在后
				ImageLoader.getInstance().displayImage(url, ivPreview, ImageOption.options);
				break;
			}
			break;
		case Activity.RESULT_CANCELED:
			break;
		}
	}

}
