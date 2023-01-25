package com.dashlane.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.module.AppGlideModule;

import java.io.File;

public class DashlaneGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(final Context context, GlideBuilder builder) {
        builder.setDiskCache(() -> {
            File cacheLocation = new File(context.getCacheDir(), "imagecache");
            cacheLocation.mkdirs();
            return DiskLruCacheWrapper.get(cacheLocation, 100 * 1024 * 1024);
        })
        .setDefaultTransitionOptions(Drawable.class, DrawableTransitionOptions.withCrossFade())
        .setDefaultTransitionOptions(Bitmap.class, BitmapTransitionOptions.withCrossFade());
    }
}
