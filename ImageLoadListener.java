package com.example.imageloader;

import java.lang.ref.WeakReference;

import com.example.imageloader.ImageLoader.ImageLoaderListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

/**
 * 图片加载监听
 * 
 * @author simen
 * 
 */
public class ImageLoadListener implements ImageLoaderListener {
	WeakReference<ImageView> mImageView;
	Context mContext;
	boolean isBg = false;
	boolean scaleToScreenWidth = false;
	float scale;
	String url;

	public String getUrl() {
		return url;
	}

	public ImageLoadListener(ImageView image, Context context, String url) {
		init(image, context, url, false, 0f);
	}

	public ImageLoadListener(ImageView image, Context context, String url, boolean isBg) {
		init(image, context, url, isBg, 0f);
	}

	public ImageLoadListener(ImageView image, Context context, String url, boolean isBg, float scale) {
		init(image, context, url, isBg, scale);
	}

	public void init(ImageView image, Context context, String url, boolean isBg, float scale) {
		image.setImageResource(StaticConstant.IMG_SRC_ING);
		this.url = url;
		image.setTag(url);
		mImageView = new WeakReference<ImageView>(image);
		mContext = context;
		this.isBg = isBg;
		this.scale = scale;
		if (scale > 0) {
			this.scaleToScreenWidth = true;
		}
	}

	@Override
	public void loadErro(String url) {
		if (mImageView.get() != null) {
			ImageView image = mImageView.get();
			if (isBg) {
				image.setBackgroundResource(StaticConstant.IMG_SRC_ERRO);
			} else {
				image.setImageResource(StaticConstant.IMG_SRC_ERRO);
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void loadSuccess(Bitmap bitmap, String url) {
		if (mImageView.get() != null) {
			ImageView image = mImageView.get();
			if (!url.equals((String) image.getTag()))
				return;

			if (bitmap != null) {
				if (isBg) {
					setBackgroundDrawable(mContext, image, new BitmapDrawable(bitmap));
				} else {
					if (scaleToScreenWidth)
						image.setImageBitmap(scale(bitmap, mContext, scale));
					else
						image.setImageBitmap(bitmap);
				}
			} else {
				if (isBg) {
					image.setBackgroundResource(StaticConstant.IMG_SRC_ERRO);
				} else {
					image.setImageResource(StaticConstant.IMG_SRC_ERRO);
				}
			}
		}
	}

	/**
	 * bitmap宽高拉伸
	 * 
	 * @param b
	 * @param context
	 * @param scale
	 *            拉伸为屏幕的多少倍
	 * @return
	 * @author simen
	 */
	public static Bitmap scale(Bitmap b, Context context, float scale) {
		float x = context.getResources().getDisplayMetrics().widthPixels * scale;
		int w = b.getWidth();
		int h = b.getHeight();
		float sx = (float) x / w;
		Matrix matrix = new Matrix();
		matrix.postScale(sx, sx); // 长和宽放大缩小的比例
		Bitmap resizeBmp = Bitmap.createBitmap(b, 0, 0, w, h, matrix, true);
		return resizeBmp;
	}

	@SuppressLint("NewApi")
	public static void setBackgroundDrawable(Context context, View view, Drawable drawable) {
		if (android.os.Build.VERSION.SDK_INT < 16) {
			view.setBackgroundDrawable(drawable);
		} else {
			view.setBackground(drawable);
		}

	}
}
