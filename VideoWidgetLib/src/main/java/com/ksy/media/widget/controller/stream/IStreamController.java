package com.ksy.media.widget.controller.stream;

import com.ksy.media.widget.controller.base.IMediaPlayerBaseControl;


public interface IStreamController extends IMediaPlayerBaseControl {

    int getPlayMode();

    void onRequestPlayMode(int requestPlayMode);

    void onBackPress(int playMode);

    void onControllerShow(int playMode);

    void onControllerHide(int playMode);

    void onRequestLockMode(boolean lockMode);
}
