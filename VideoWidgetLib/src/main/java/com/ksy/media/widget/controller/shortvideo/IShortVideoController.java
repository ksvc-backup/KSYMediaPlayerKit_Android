package com.ksy.media.widget.controller.shortvideo;

import com.ksy.media.widget.controller.base.IMediaPlayerBaseControl;


public interface IShortVideoController extends IMediaPlayerBaseControl {

    void onBackPress(int playMode);

    void onControllerShow(int playMode);

    void onControllerHide(int playMode);
}
