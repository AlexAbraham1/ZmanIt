package com.alexabraham.zmanit.app;

import android.content.Intent;

import java.util.Date;

public class ZmanItem {
	
	public static final String ITEM_SEP = System.getProperty("line.separator");
	
	public final static String TITLE = "mTitleView";
	public final static String TIME = "time";
	
	private String mTitle = new String();
	private Date mTime = new Date();
	
	ZmanItem(String title, Date time){
		mTitle = title;
		mTime = time;
	}
	
	ZmanItem(Intent intent){

		mTitle = intent.getStringExtra(TITLE);
		mTime = new Date();
        mTime.setTime(intent.getLongExtra(TIME, -1));
	}

	public String getTitle() {
		return mTitle;
	}
	
	public void setTitle(String title){
		mTitle = title;
	}

	public Date getTime() {
		return mTime;
	}
	
	public void setTime(Date time){
		mTime = time;
	}
	
	public static void packageIntent(Intent intent, String title, Date time){
		intent.putExtra(ZmanItem.TITLE, title);
		intent.putExtra(ZmanItem.TIME, time.getTime());
	}
	
	public String toString(){
		return mTitle + ITEM_SEP + mTime.getTime();
	}
	
	public String toLog() {
		return "Title:" + mTitle + ITEM_SEP + "Time:" + mTime;
	}

}
