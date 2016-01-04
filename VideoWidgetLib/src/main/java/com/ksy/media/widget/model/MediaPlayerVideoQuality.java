package com.ksy.media.widget.model;

public enum MediaPlayerVideoQuality {

    UNKNOWN(MediaPlayerVideoQuality.VIDEO_QUALITY_UNKNOWN,"未知"),HD(MediaPlayerVideoQuality.VIDEO_QUALITY_HD,"高清"), SD(MediaPlayerVideoQuality.VIDEO_QUALITY_SD,"标清");
    
    public static final int VIDEO_QUALITY_UNKNOWN = -1;
    public static final int VIDEO_QUALITY_HD = 1;
    public static final int VIDEO_QUALITY_SD = 2;
    
    private MediaPlayerVideoQuality(int flag, String name) {
        this.flag = flag;
        this.name = name;
    }
    
    private int flag = VIDEO_QUALITY_UNKNOWN;
    private String name;
    public int getFlag() {
        return flag;
    }
    public String getName() {
        return name;
    }
    
    public static MediaPlayerVideoQuality getQualityNameByFlag(int flag){
        switch (flag) {
		case VIDEO_QUALITY_UNKNOWN:
			return UNKNOWN;
		case VIDEO_QUALITY_HD:
			return HD;
		case VIDEO_QUALITY_SD:
			return SD;
		}
        return null;
        
    }
    
}
