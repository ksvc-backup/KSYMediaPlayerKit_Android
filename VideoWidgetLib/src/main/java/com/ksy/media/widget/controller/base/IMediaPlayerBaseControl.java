package com.ksy.media.widget.controller.base;

public interface IMediaPlayerBaseControl {

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
}
