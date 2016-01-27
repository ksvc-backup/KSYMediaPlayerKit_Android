package com.ksy.media.widget.util;

import java.io.File;

import android.util.Log;

public class IOUtils {

	public static void doDeleteEmptyDir(String dir) {

		boolean success = (new File(dir)).delete();
		if (success) {
			Log.i(Constants.LOG_TAG, "Successfully deleted empty directory: " + dir);
		} else {
			Log.e(Constants.LOG_TAG, "Failed to delete empty directory: " + dir);
		}
	}

	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();// 递归删除目录中的子目录下
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					boolean success = deleteDir(new File(dir, children[i]));
					if (!success) {
						return false;
					}
				}
			} else {
				Log.e(Constants.LOG_TAG, "children can not null");
			}
		}
		
		// 目录此时为空，可以删除
		return dir.delete();
	}
}
