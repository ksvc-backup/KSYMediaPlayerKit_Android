package com.ksy.media.widget.controller;

/**
 * Created by LIXIAOPENG on 2015/12/21.
 */
public interface MediaPlayerController extends IMediaPlayerControl {

    boolean supportQuality();

    boolean supportVolume();

    boolean playVideo(String url);

    int getPlayMode();

    void onRequestPlayMode(int requestPlayMode);

    void onBackPress(int playMode);

    void onControllerShow(int playMode);

    void onControllerHide(int playMode);

    void onRequestLockMode(boolean lockMode);

    void onVideoPreparing();

    void onMovieRatioChange(int screenSize);

    void onMoviePlayRatioUp();

    void onMoviePlayRatioDown();

    void onMovieCrop();

    void onVolumeDown();

    void onVolumeUp();
}
