package com.ksy.media.widget.ui.video;

/**
 * @description 相关信息
 * @author LIXIAOPENG
 * 
 */
public class VideoRelateVideoInfo {
    
	/**
	 * 视频的长度
	 */
    private int duration;
    
	/**
     * 视频ID  
     */
    private int id;
    
    /**
     * 视频名字  
     */
    private String displayName;
    
    /**
     * 视频地址
     */
    private String path;
    
    /**
     * 主演
     */
    private String mActor;
    
    /**
     * 影片简介
     */
    private String summary;
    
    /**
     * 视频海报地址
     */
    private String mPicUrl;

    
	public String getmPicUrl() {
		return mPicUrl;
	}

	public void setmPicUrl(String mPicUrl) {
		this.mPicUrl = mPicUrl;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getmActor() {
		return mActor;
	}

	public void setmActor(String mActor) {
		this.mActor = mActor;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

    public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
