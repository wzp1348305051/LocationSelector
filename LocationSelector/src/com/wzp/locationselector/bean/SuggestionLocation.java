package com.wzp.locationselector.bean;

import com.baidu.mapapi.model.LatLng;

public class SuggestionLocation {
	private boolean selected;// 该地点是否被选中
	private String name;// poi结果名称
	private String address;// poi结果地址
	private LatLng location;// poi结果经纬度
	
	public void SetSelected(boolean selected) {
		this.selected = selected;
	}
	
	public boolean getSelected() {
		return selected;
	}
	
	public void SetName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void SetAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
	public void SetLocation(LatLng location) {
		this.location = location;
	}
	
	public LatLng getLocation() {
		return location;
	}
	
}
