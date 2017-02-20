package com.example.imageloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.example.http.HttpRequestRun;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ImageView;

/**
 * 图片加载
 * @author simen
 *
 */
public class ImageLoader {

	private FileCache fileCache;

	private static class SingleInstance {
		public final static ImageLoader instance = new ImageLoader(); 
	}

	
	public static ImageLoader getInstance() {
		return SingleInstance.instance;
	}

	public ImageLoader() {
		fileCache = new FileCache(StaticConstant.IMGCACHE);
	}

	/**
	 * 显示图片
	 * 
	 * @param url
	 * @param activity
	 * @param imageView
	 */
	public void DisplayImage(String url, final ImageLoaderListener lister) {
		Bitmap bitmap = BitmapManager.getBitmapFromMemory(url);
		if (bitmap != null) {
			lister.loadSuccess(bitmap, url);
		} else {
			loaderImage(url, lister);
		}
	}

	private void loaderImage(String url, ImageLoaderListener lister) {
		BitmapLoadExecutorService.getInstance().addHttpRequestQueue(imageLoader(url, lister));
	}

	private Bitmap getBitmap(String url) {

		File f = fileCache.getFile(url, StaticConstant.IMGVERSION);

		// from SD cache
		Bitmap b = decodeFile(f);

		if (b != null) {
			return b;
		}

		try {
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(15000);
			conn.setReadTimeout(15000);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			is.close();
			bitmap = decodeFile(f);
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} catch (OutOfMemoryError e2) {
			clearCache();
			return null;
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {

		try {

			return BitmapFactory.decodeStream(new FileInputStream(f));

		} catch (FileNotFoundException e) {

		} catch (OutOfMemoryError e) {

			try {
				// decode image size
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(new FileInputStream(f), null, o);

				// Find the correct scale value. It should be the power of 2.
				final int REQUIRED_SIZE = 70;
				int width_tmp = o.outWidth, height_tmp = o.outHeight;
				int scale = 1;
				while (true) {
					if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
						break;
					width_tmp /= 2;
					height_tmp /= 2;
					scale *= 2;
				}
				// decode with inSampleSize
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;
				return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (OutOfMemoryError e2) {
				clearCache();
			}
		}
		return null;
	}

	private Thread imageLoader(final String url, final ImageLoaderListener lister) {
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (lister != null) {
					if (msg.obj != null) {
						lister.loadSuccess((Bitmap) msg.obj, url);
					} else {
						lister.loadErro(url);
					}
				}
				super.handleMessage(msg);
			}
		};

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Bitmap bmp = getBitmap(url);
				BitmapManager.addBitmapToMemoryCache(url, bmp);
				sendToHandler(handler, bmp);
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY - 1);
		return thread;
	}

	/**
	 * 消息发送
	 * 
	 * @param handler
	 * @param msg
	 */
	public void sendToHandler(Handler handler, Bitmap msg) {
		Message message = handler.obtainMessage();
		message.obj = msg;
		handler.sendMessage(message);
	}

	public static void clearCache() {
		BitmapManager.clearCache();
	}

	public interface ImageLoaderListener {
		void loadSuccess(Bitmap bitmap, String url);

		void loadErro(String url);
	}
}
