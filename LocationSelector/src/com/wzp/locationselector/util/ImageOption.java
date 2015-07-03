package com.wzp.locationselector.util;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.wzp.locationselector.R;

public class ImageOption {
	public static DisplayImageOptions options;
	static {
		options = new DisplayImageOptions.Builder()  
		.showImageOnLoading(R.drawable.ic_launcher) //设置图片在下载期间显示的图片  
		.showImageForEmptyUri(R.drawable.ic_launcher)//设置图片Uri为空或是错误的时候显示的图片  
		.showImageOnFail(R.drawable.ic_launcher)  //设置图片加载/解码过程中错误时候显示的图片
		.cacheInMemory(true)//设置下载的图片是否缓存在内存中  
		.cacheOnDisk(true)//设置下载的图片是否缓存在SD卡中  
		.considerExifParams(true)  //是否考虑JPEG图像EXIF参数（旋转，翻转）
		.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)//设置图片以如何的编码方式显示  
		.resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位  
		.build();//构建完成  
	}
}
