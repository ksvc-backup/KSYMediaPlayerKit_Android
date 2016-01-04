package com.ksy.media.widget.util;

import java.io.File;

import android.util.Log;

public class IOUtils {

	/**
	 * 删除空目录
	 * 
	 * @param dir
	 *            将要删除的目录路径
	 */
	public static void doDeleteEmptyDir(String dir) {

		boolean success = (new File(dir)).delete();
		if (success) {
			Log.i(Constants.LOG_TAG, "Successfully deleted empty directory: " + dir);
		} else {
			Log.e(Constants.LOG_TAG, "Failed to delete empty directory: " + dir);
		}
	}

	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 * 
	 * @param dir
	 *            将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful. If a
	 *         deletion fails, the method stops attempting to delete and returns
	 *         "false".
	 */
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
