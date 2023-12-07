package com.dagnerchuman.miaplicativonegociomicroservice.activity.Fragments;

import android.graphics.Bitmap;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.DefaultImageHeaderParser;

import java.security.MessageDigest;

public class ResizeTransformation extends BitmapTransformation {
    private static final int VERSION = 1;

    private static final String ID = "com.dagnerchuman.miaplicativonegociomicroservice." + VERSION;

    private int targetWidth;
    private int targetHeight;

    public ResizeTransformation(int targetWidth, int targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        if (toTransform.getWidth() == targetWidth && toTransform.getHeight() == targetHeight) {
            return toTransform;
        }

        return Bitmap.createScaledBitmap(toTransform, targetWidth, targetHeight, true);
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(("resize(" + targetWidth + targetHeight + ")").getBytes(CHARSET));
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ResizeTransformation) {
            ResizeTransformation other = (ResizeTransformation) o;
            return targetWidth == other.targetWidth && targetHeight == other.targetHeight;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ID.hashCode() + targetWidth * 1000 + targetHeight;
    }
}
