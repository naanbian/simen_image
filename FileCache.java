package com.example.imageloader;

import java.io.File;

import android.content.Context;

/**
 * 缓存文件夹
 * 
 * @author simen
 * 
 */
public class FileCache {

	private File cacheDir;
	private String foldName;

	public FileCache(String foldName) {
		// 判断是否存在sd_card
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			this.foldName = foldName;
			cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), StaticConstant.APPNAME + File.separator + foldName);
		} else {
			// cacheDir=BaseApplication.getInstance().getCacheDir();
		}

		// 判断文件夹是否存在，否则逐级创建
		if (!cacheDir.exists())
			cacheDir.mkdirs();
	}

	public File getFile(String url, String imgVersion) {
		// I identify images by hashcode. Not a perfect solution, good for the
		// demo.
		String filename = String.valueOf(url.hashCode() + imgVersion);
		File f = new File(cacheDir, filename);
		return f;
	}

	/**
	 * 清空缓存
	 */
	public static void clear() {
		File cache = null;
		// 判断是否存在sd_card
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			cache = new File(android.os.Environment.getExternalStorageDirectory(), StaticConstant.APPNAME);
		} else {
			// cache = BaseApplication.getInstance().getCacheDir();
		}

		if (cache != null && cache.exists()) {
			File[] childFile = cache.listFiles();
			for (File f : childFile) {
				clearFold(f);
			}
		}
		BitmapManager.clearCache();// 清空图片缓存
	}

	public static void clearFold(File file) {
		if (file != null && file.exists()) {
			if (file.isFile()) {
				file.delete();
				return;
			}
			if (file.isDirectory()) {
				File[] childFile = file.listFiles();
				if (childFile == null || childFile.length == 0) {
					file.delete();
					return;
				}
				for (File f : childFile) {
					clearFold(f);
				}
			}
		}
	}

	public void clear(String url) {
		File f = new File(cacheDir, String.valueOf(url.hashCode()));
		if (f.exists())
			f.delete();
	}

}