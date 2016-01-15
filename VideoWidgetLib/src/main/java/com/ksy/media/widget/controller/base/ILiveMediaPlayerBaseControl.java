package com.ksy.media.widget.controller.base;

public interface ILiveMediaPlayerBaseControl {

	void start();

	void pause();

	boolean isPlaying();

	boolean canStart();

	boolean canPause();

	void onPlay();

	void onPause();

}
