package com.dashlane.util.graphics;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.dashlane.url.UrlDomain;
import com.dashlane.url.UrlDomainUtils;
import com.dashlane.url.icon.UrlDomainIcon;
import com.dashlane.url.icon.UrlDomainIconAndroidRepository;
import com.dashlane.url.icon.UrlDomainIconComponent;
import com.dashlane.util.LoggerKt;

import java.lang.ref.WeakReference;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.flow.Flow;

import static com.dashlane.util.LoggerKt.logW;

public class RemoteImageRoundRectDrawable extends RoundRectDrawable {

    private Listener mListener;

    private Drawable mPlaceholder;

    private final ImageDownloadedTarget mTarget;

    private final Context mContext;

    public RemoteImageRoundRectDrawable(Context context, int backgroundColor) {
        super(context, backgroundColor);
        mContext = context;
        mTarget = new ImageDownloadedTarget(this);
    }

    public Target<Drawable> getTarget() {
        return mTarget;
    }

    public void loadImage(String url, Drawable placeholder) {
        UrlDomainIconAndroidRepository iconRepository =
                UrlDomainIconComponent.Companion.from(mContext.getApplicationContext()).getUrlDomainIconAndroidRepository();
        mPlaceholder = placeholder;
        
        setPlaceholder();
        if (url != null) {
            UrlDomain urlDomain = UrlDomainUtils.toUrlDomainOrNull(url);
            if (urlDomain != null) {
                Flow<UrlDomainIcon> flow = iconRepository.get(urlDomain);
                RemoteImageRoundRectDrawableHelperKt.launchCollect(mContext, flow, new DrawableIconCallback(this));
            }
        }
    }

    private void loadImage(String url, int backgroundColor) {
        
        if (mPlaceholder instanceof BackgroundColorDrawable) {
            ((BackgroundColorDrawable) mPlaceholder).setBackgroundColor(backgroundColor);
        }
        if (url == null) {
            setPlaceholder();
        } else {
            Glide.with(mContext)
                 .load(url)
                 .placeholder(mPlaceholder)
                 .into(mTarget);
        }
    }

    private void setPlaceholder() {
        Glide.with(mContext).clear(mTarget);
        setImage(mPlaceholder);
    }

    public void setListener(Listener listener) {
        mListener = listener;
        if (mListener != null) {
            mListener.onImageChanged(this, getImage(), getBackgroundColor());
        }
    }

    @Override
    public void setImage(Drawable drawable) {
        super.setImage(drawable);
        if (mListener != null) {
            mListener.onImageChanged(this, drawable, getBackgroundColor());
        }
    }

    public interface Listener {
        void onImageChanged(RemoteImageRoundRectDrawable parent, Drawable drawable, int backgroundColor);
    }

    private static class ImageDownloadedTarget extends CustomTarget<Drawable> {

        private RemoteImageRoundRectDrawable mRoundRectDrawable;

        private ImageDownloadedTarget(RemoteImageRoundRectDrawable roundRectDrawable) {
            mRoundRectDrawable = roundRectDrawable;
        }

        @Override
        public void onLoadStarted(Drawable placeholder) {
            if (placeholder != null) {
                mRoundRectDrawable.setImage(placeholder);
            }
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {
            
        }

        @Override
        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
            mRoundRectDrawable.setImage(resource);
        }
    }

    private static class DrawableIconCallback implements Function1<UrlDomainIcon, Unit> {
        private WeakReference<RemoteImageRoundRectDrawable> mWeakReference;

        DrawableIconCallback(RemoteImageRoundRectDrawable drawable) {
            mWeakReference = new WeakReference<>(drawable);
        }

        @Override
        public Unit invoke(UrlDomainIcon urlDomainIcon) {
            String url = urlDomainIcon.getUrl();
            if (url == null) return Unit.INSTANCE;
            Integer backgroundColor = RemoteImageRoundRectDrawableHelperKt.getBackgroundColor(urlDomainIcon);
            if (backgroundColor == null) return Unit.INSTANCE;
            loadImage(url, backgroundColor);
            return Unit.INSTANCE;
        }

        private void loadImage(String url, @ColorInt int backgroundColor) {
            RemoteImageRoundRectDrawable drawable = mWeakReference.get();
            if (drawable != null) {
                
                try {
                    drawable.loadImage(url, backgroundColor);
                } catch (Exception e) {
                    logW("UrlDomainIcon", "Error while loading image.", e);
                }
            }
        }
    }
}
