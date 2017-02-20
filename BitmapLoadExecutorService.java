package com.example.imageloader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * 图片下载线程池
 * @author simen
 *
 */
public class BitmapLoadExecutorService {
	
	BitmapCache cache;
	ExecutorService service;// 线程池
	
	BitmapLruCache mMemoryCache;

	/**
	 * 内部类
	 * 
	 * @author simen
	 */
	private static class GetInstance {
		public static BitmapLoadExecutorService asyncRequest = new BitmapLoadExecutorService();
	}

	/**
	 * 内部类加载单例
	 * 
	 * @return
	 */
	public static BitmapLoadExecutorService getInstance() {
		return GetInstance.asyncRequest;
	}

	/**
	 * 初始化 定长线程池，最大并发2，多则等待
	 */
	public BitmapLoadExecutorService() {
		service = Executors.newFixedThreadPool(2);//为了加载顺畅度，减少线程池并数
		cache = new BitmapCache();
		mMemoryCache = new BitmapLruCache((int) (Runtime.getRuntime().maxMemory() / 1024)/8);
	}

	public void stop() {
		try {
			if (service != null) {
				service.shutdown();
				service.shutdownNow();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initService() {
		try {
			if (service != null) {
				service = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 图片下载，加入线程池管理队列
	 * 
	 * @param url
	 */
	public void addHttpRequestQueue(Thread item) {
		service.execute(item);
	}
	
	public BitmapLruCache getMemoryCache(){
		return mMemoryCache;
	}
	
	public BitmapCache getBitmapCache(){
		return cache;
	}
	
	/**
	 * 
	 * @author simen
	 *  图片一级缓存
	 */
	@SuppressLint("NewApi")
	public class BitmapLruCache extends LruCache<String, Bitmap>{
		
		public BitmapLruCache(int maxSize) {
			super(maxSize);
		}
		
		@Override  
        protected int sizeOf(String key, Bitmap bitmap) { 
        	return bitmap.getByteCount() / 1024;  
        }  
		
		/**
		 * 回收处理
		 * 回收时加入到软引用二级缓存
		 */
		@Override
		protected void entryRemoved(boolean evicted, String key,
				Bitmap oldValue, Bitmap newValue) {
			super.entryRemoved(evicted, key, oldValue, newValue);
			if(evicted){
				cache.addCacheBitmap(key, oldValue);
//				if(oldValue!=null&&!oldValue.isRecycled()){
//					oldValue.recycle();
//				}
//				oldValue = null;
			}
		}
		
	}
	
}
