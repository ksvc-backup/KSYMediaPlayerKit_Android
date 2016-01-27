package com.ksy.media.widget.util;

import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.Locale;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ksy.media.widget.model.MediaPlayMode;

public class MediaPlayerUtils {

	public static final int DEVICE_NATURAL_ORIENTATION_PORTRAIT = 0;
	public static final int DEVICE_NATURAL_ORIENTATION_LANDSCAPE = 1;

	public static final int DEVICE_NAVIGATION_TYPE_UNKNOWN = 0;
	public static final int DEVICE_NAVIGATION_TYPE_HANDSET = 1;
	public static final int DEVICE_NAVIGATION_TYPE_TABLET = 2;
	private static StringBuilder mFormatBuilder;
	private static Formatter mFormatter;

	static {
		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
	}

	
	public static String getVideoDisplayTime(long timeMs) {

		int totalSeconds = (int) timeMs / 1000;

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		mFormatBuilder.setLength(0);

//		if (hours > 0) {
//			return mFormatter.format("%02d:%02d", minutes, seconds).toString();
//		} else {
//			return mFormatter.format("%02d:%02d", minutes, seconds).toString();
//		}
		

		if (hours > 0) {
			return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
		} else {
			return mFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
		}

	}

	public static int getRealDisplayHeight(Window window) {
		int height = 0;
		Display display = window.getWindowManager().getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		try {
			@SuppressWarnings("rawtypes")
			Class clazz = Class.forName("android.view.Display");
			@SuppressWarnings("unchecked")
			Method method = clazz.getMethod("getRealMetrics",DisplayMetrics.class);
			method.invoke(display, displayMetrics);
			height = displayMetrics.heightPixels;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return height;
	}

	public static int getRealDisplayWidth(Window window) {
		int width = 0;
		Display display = window.getWindowManager().getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		try {
			@SuppressWarnings("rawtypes")
			Class clazz = Class.forName("android.view.Display");
			@SuppressWarnings("unchecked")
			Method method = clazz.getMethod("getRealMetrics",DisplayMetrics.class);
			method.invoke(display, displayMetrics);
			width = displayMetrics.widthPixels;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return width;
	}

	public static boolean isFullScreenMode(int playMode) {
		return (playMode == MediaPlayMode.PLAY_MODE_FULLSCREEN ? true : false);
	}

	public static boolean isWindowMode(int playMode) {
		return (playMode == MediaPlayMode.PLAY_MODE_WINDOW ? true : false);
	}

	public static void hideSystemUI(Window window, boolean uiOverlay) {

		if (Build.VERSION.SDK_INT >= 16) {
			window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
		} else if (Build.VERSION.SDK_INT >= 14) {
			window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}

	}

	public static int dip2px(Context context, float dipValue) {
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int sp2px(Context context, float spValue) { 
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity; 
        return (int) (spValue * fontScale + 0.5f); 
    } 
	
	
	public static int getXLocationOnScreen(View view) {
		if (view == null)
			throw new NullPointerException("view can't be null !!");
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		return location[0];
	}

	public static int getYLocationOnScreen(View view) {
		if (view == null)
			throw new NullPointerException("view can't be null !!");
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		return location[1];
	}

	public static void showSystemUI(Window window, boolean uiOverlay) {

		if (Build.VERSION.SDK_INT >= 16) {
			window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			if (uiOverlay) {
				// window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
			} else {
				window.getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_VISIBLE);
			}
		} else if (Build.VERSION.SDK_INT >= 14) {
			window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			if (uiOverlay) {
				// window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
			} else {
				window.getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_VISIBLE);
			}
		}

	}

	public static int getDeviceNaturalOrientation(Window window) {

		Display display;
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display = window.getWindowManager().getDefaultDisplay();
		display.getMetrics(displayMetrics);
		int rotation = display.getRotation();
		int width = 0;
		int height = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
		case Surface.ROTATION_180:
			width = displayMetrics.widthPixels;
			height = displayMetrics.heightPixels;
			break;
		case Surface.ROTATION_90:
		case Surface.ROTATION_270:
			width = displayMetrics.heightPixels;
			height = displayMetrics.widthPixels;
			break;
		default:
			break;
		}

		if (width > height) {
			return DEVICE_NATURAL_ORIENTATION_LANDSCAPE;
		} else {
			return DEVICE_NATURAL_ORIENTATION_PORTRAIT;
		}

	}

	public static int getUsedDisplayWidth(Window window) {
		int width = 0;
		DisplayMetrics displayMetrics = new DisplayMetrics();
		window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		width = displayMetrics.widthPixels;
		return width;
	}

	public static int getUsedDisplayHeight(Window window) {
		int height = 0;
		DisplayMetrics displayMetrics = new DisplayMetrics();
		window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		height = displayMetrics.heightPixels;
		return height;
	}

	/*
	 * 获取机器本身的navigation type 
	 * value : handset & tablet
	 */
	public static int getDeviceNavigationType(Window window) {

		int deviceNavigationType = DEVICE_NAVIGATION_TYPE_UNKNOWN;
		int usedDisplayWidth = getUsedDisplayWidth(window);
		int usedDisplayHeight = getUsedDisplayHeight(window);
		int realDisplayWidth = getRealDisplayWidth(window);
		int realDisplayHeight = getRealDisplayHeight(window);
		Log.i(Constants.LOG_TAG, "usedDisplayWidth :" + usedDisplayWidth + ", usedDisplayHeight :" + usedDisplayHeight);
		Log.i(Constants.LOG_TAG, "realDisplayWidth :" + realDisplayWidth + ", realDisplayHeight :" + realDisplayHeight);
		
		if (usedDisplayWidth < realDisplayWidth) {
			deviceNavigationType = DEVICE_NAVIGATION_TYPE_HANDSET;
		} else if (usedDisplayHeight < realDisplayHeight) {
			deviceNavigationType = DEVICE_NAVIGATION_TYPE_TABLET;
		}
		return deviceNavigationType;

	}

	public static boolean hasNavigationBar(Window window) {

		int usedDisplayWidth = getUsedDisplayWidth(window);
		int usedDisplayHeight = getUsedDisplayHeight(window);
		int realDisplayWidth = getRealDisplayWidth(window);
		int realDisplayHeight = getRealDisplayHeight(window);

		if (usedDisplayWidth < realDisplayWidth || usedDisplayHeight < realDisplayHeight)
			return true;
		return false;

	}

	public static int getNavigationBarHeight(Window window) {
		int navigationBarHeight = 0;
		int usedDisplayWidth = getUsedDisplayWidth(window);
		int usedDisplayHeight = getUsedDisplayHeight(window);
		int realDisplayWidth = getRealDisplayWidth(window);
		int realDisplayHeight = getRealDisplayHeight(window);
		if (usedDisplayWidth < realDisplayWidth) {
			navigationBarHeight = realDisplayWidth - usedDisplayWidth;
		} else if (usedDisplayHeight < realDisplayHeight) {
			navigationBarHeight = realDisplayHeight - usedDisplayHeight;
		}
		return navigationBarHeight;
	}

	public static boolean checkSystemGravity(Context context) {
		try {
			int systemGravity = Settings.System.getInt(context.getContentResolver(),Settings.System.ACCELEROMETER_ROTATION);
			if (systemGravity == 1)
				return true;
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
}
