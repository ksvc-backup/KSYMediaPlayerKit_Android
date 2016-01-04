package com.ksy.media.widget.util;

public class PlayConfig {

    public static final int INTERRUPT_MODE_RELEASE_CREATE = 0;
    public static final int INTERRUPT_MODE_PAUSE_RESUME = 1;
    public static final int INTERRUPT_MODE_STAY_PLAYING = 2;
    public boolean isStream = false;
    public int interruptMode = INTERRUPT_MODE_RELEASE_CREATE;

    public boolean isStream() {
        return isStream;
    }

    public void setStream(boolean isStream) {
        this.isStream = isStream;
    }

    public int getInterruptMode() {
        return interruptMode;
    }

    public void setInterruptMode(int interruptMode) {
        this.interruptMode = interruptMode;
    }

    private static PlayConfig mInstance;
    private static Object mLockObject = new Object();

    public static PlayConfig getInstance() {
        if (null == mInstance) {
            synchronized (mLockObject) {
                if (null == mInstance) {
                    mInstance = new PlayConfig();
                }
            }
        }
        return mInstance;
    }

}
