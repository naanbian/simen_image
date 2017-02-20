package com.example.imageloader;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Hashtable;

import android.graphics.Bitmap;

/**
 * bitmap二级缓存处理
 * @author simen
 */
public class BitmapCache {
	
	public BitmapCache() {
		hashRefs = new Hashtable<String, MySoftRef>();
		q = new ReferenceQueue<Bitmap>();
	}

	/**
	 * bitmap软引用
	 */
	private Hashtable<String, MySoftRef> hashRefs;


	/**
	 * 引用队列，当软引用被回收时会被加入到引用队列
	 */
	private ReferenceQueue<Bitmap> q;

	/**
	 * 软引用加入引用队列处理
	 * @author simen
	 *
	 */
	private class MySoftRef extends SoftReference<Bitmap> {

		private String _key = null;

		public MySoftRef(Bitmap bmp, ReferenceQueue<Bitmap> q, String key) {
			super(bmp, q);
			_key = key;
		}
	}

    public Bitmap getBitmap(String key) {
        Bitmap bmp = null;
        //从软引用拿到bitmap之后将其移除，因为后面会将这个bitmap加入到lrucache当中
         if (hashRefs.containsKey(key)) {
            MySoftRef ref = (MySoftRef) hashRefs.get(key);
            bmp = (Bitmap) ref.get();
           hashRefs.remove(key);
        }
        return bmp;

    }

	/**
	 * bitmap增加到cache
	 * @param key
	 * @param bmp
	 */
	public void addCacheBitmap(String key,Bitmap bmp) {
		cleanCache();
		MySoftRef ref = new MySoftRef(bmp, q, key);
		hashRefs.put(key, ref);
	}
	
	
	/**
	 * 移除已被回收的引用，节省空间
	 */
	private void cleanCache() {
		MySoftRef ref = null;
		while ((ref = (MySoftRef) q.poll()) != null) {
			hashRefs.remove(ref._key);
		}
	}
	
	/**
	 * 
	 * @param key
	 */
	public void removeItem(String key){
		hashRefs.remove(key);
	}

	/**
	 * 清空cache
	 */
	public void clearCache() {
		hashRefs.clear();
		cleanCache();
	}
}
