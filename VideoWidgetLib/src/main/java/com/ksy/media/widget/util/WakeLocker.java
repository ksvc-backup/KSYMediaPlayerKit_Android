package com.ksy.media.widget.util;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class WakeLocker {

    private static WakeLock mWakeLock;

    public static void acquire(Context context) {
        if (mWakeLock == null) {
            PowerManager powerManager = (PowerManager) (context.getSystemService(Context.POWER_SERVICE));
            int level = PowerManager.SCREEN_BRIGHT_WAKE_LOCK;
            int flag = PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE;
            mWakeLock = powerManager.newWakeLock(level | flag, context.getPackageName());
        }
        if(!mWakeLock.isHeld()){
            mWakeLock.acquire();
        }
    }

    public static void release() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }
    
    public static boolean isScreenOn(Context context){
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }
}
