package com.wzp.locationselector.bean;

import com.baidu.mapapi.model.LatLng;

/**
 * 搜索结果类
 * */
public class SearchResult {
	private String name;// poi结果名称
	private String address;// poi结果地址
	private LatLng location;// poi结果经纬度
	
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
