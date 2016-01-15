package com.ksy.media.widget.controller.video;

import com.ksy.media.widget.controller.base.IMediaPlayerBaseControl;


public interface IVideoController extends IMediaPlayerBaseControl {

    int getPlayMode();

    void onRequestPlayMode(int requestPlayMode);

    void onBackPress(int playMode);

    void onControllerShow(int playMode);

    void onControllerHide(int playMode);

    void onRequestLockMode(boolean lockMode);

    void onMovieRatioChange(int screenSize);

    void onMovieCrop();
}
