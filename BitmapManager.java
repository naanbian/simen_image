package com.example.imageloader;

import android.graphics.Bitmap;


/**
 * 图片缓存管理类
 * @author simen
 *
 */
public class BitmapManager {  
    
	/**
	 * 加入到缓存
	 * @param key
	 * @param bitmap
	 */
    public static void addBitmapToMemoryCache(String key, Bitmap bitmap) { 
        if (getBitmapFromMemory(key) == null&&bitmap!=null) {
            BitmapLoadExecutorService.getInstance().getMemoryCache().put(key, bitmap); 
            bitmap = null;
        } 
    } 
    
    /**
     * 从缓存读取
     * @param url
     * @return
     */
	public static Bitmap getBitmapFromMemory(String url) {
		Bitmap bitmap = null;

		bitmap = BitmapLoadExecutorService.getInstance().getMemoryCache().get(url);
		if (bitmap != null) {
			return bitmap;
		}else{
			bitmap = BitmapLoadExecutorService.getInstance().getBitmapCache().getBitmap(url);
			if (bitmap != null) {
				BitmapLoadExecutorService.getInstance().getMemoryCache().put(url, bitmap);
			}
			return bitmap;
		}
	}
	
	/**
	 * 清空图片缓存
	 */
	public static void clearCache(){
		BitmapLoadExecutorService.getInstance().getMemoryCache().evictAll();
		BitmapLoadExecutorService.getInstance().getBitmapCache().clearCache();
	}
  
}
