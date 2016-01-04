package com.ksy.media.widget.controller;

public interface IMediaPlayerControl {

	void start();

	void pause();

	int getDuration();

	int getCurrentPosition();

	void seekTo(long pos);

	boolean isPlaying();

	int getBufferPercentage();

	boolean canStart();

	boolean canPause();

	boolean canSeekBackward();

	boolean canSeekForward();

	void onPlay();

	void onPause();

}
