package com.dashlane.ui.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;



public class RemoteAvatarDownloader extends CustomTarget<Drawable> {

    @NonNull
    private final WeakReference<RemoteImageReady> mListenerReference;

    public RemoteAvatarDownloader(RemoteImageReady listener) {
        mListenerReference = new WeakReference<>(listener);
    }

    public void download(Context context, String url, Drawable placeholder) {
        RemoteImageReady listener = mListenerReference.get();
        if (url == null) {
            Glide.with(context).clear(this);
            if (listener != null) {
                listener.setImage(placeholder);
            }
        } else {
            try {
                Glide.with(context)
                     .load(url)
                     .placeholder(placeholder)
                     .transform(new CircleCrop())
                     .into(this);
            } catch (Exception e) {
                
            }
        }
    }

    @Override
    public void onLoadStarted(Drawable placeholder) {
        RemoteImageReady listener = mListenerReference.get();
        if (listener == null) {
            return;
        }
        listener.setImage(placeholder);
    }

    @Override
    public void onLoadCleared(@Nullable Drawable placeholder) {
        
    }

    @Override
    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
        RemoteImageReady listener = mListenerReference.get();
        if (listener == null) {
            return;
        }
        listener.setImage(resource);
    }

    public interface RemoteImageReady {
        RemoteAvatarDownloader getTarget();

        void setImage(Drawable drawable);
    }
}
