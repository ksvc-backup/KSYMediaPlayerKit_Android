package com.ksy.media.widget.model;


public enum MediaPlayerMovieRatio {

    WIDESCREEN(MediaPlayerMovieRatio.VIDEO_MOVIE_RATIO_16_9,"16:9"), NORMAL(MediaPlayerMovieRatio.VIDEO_MOVIE_RATIO_4_3,"4:3");
    
    public static final int VIDEO_MOVIE_RATIO_16_9 = 1;
    public static final int VIDEO_MOVIE_RATIO_4_3 = 2;
    
    private MediaPlayerMovieRatio(int flag, String name) {
        this.flag = flag;
        this.name = name;
    }
    
    private int flag = VIDEO_MOVIE_RATIO_16_9;
    private String name;
    
    public int getFlag() {
        return flag;
    }
    public String getName() {
        return name;
    }
    
    public static MediaPlayerMovieRatio getQualityNameByFlag(int flag){
        switch (flag) {
		case VIDEO_MOVIE_RATIO_16_9:
			return WIDESCREEN;
		case VIDEO_MOVIE_RATIO_4_3:
			return NORMAL;
		}
        return null;
        
    }
    
}
