package com.dashlane.ui.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.dashlane.ui.util.RemoteAvatarDownloader;
import com.dashlane.util.GravatarFetcher;

import java.util.Objects;

@Deprecated(forRemoval = true, since = "2024-03-25")
public class ContactDrawable extends Drawable implements RemoteAvatarDownloader.RemoteImageReady {

    private String mEmail;
    private Drawable mDrawable;
    private final RemoteAvatarDownloader mRemoteImageToDrawable = new RemoteAvatarDownloader(this);

    public static void toImageView(ImageView imageView, String email) {
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof ContactDrawable) {
            ((ContactDrawable) drawable).setEmail(imageView.getContext(), email);
        } else {
            imageView.setImageDrawable(newInstance(imageView.getContext(), email));
        }
    }

    public static ContactDrawable newInstance(Context context, String email) {
        ContactDrawable contactDrawable = new ContactDrawable();
        contactDrawable.setEmail(context, email);
        return contactDrawable;
    }

    private void setEmail(Context context, String email) {
        if (Objects.equals(mEmail, email)) {
            return; 
        }
        mEmail = email;
        
        setImage(CircleFirstLetterDrawable.newInstance(context, email));
        loadRemotely(context);
    }

    @Override
    public void setImage(Drawable drawable) {
        mDrawable = drawable;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mDrawable == null) {
            return;
        }
        mDrawable.setBounds(getBounds());
        mDrawable.draw(canvas);
    }

    @Override
    public void setAlpha(int i) {
        if (mDrawable != null) {
            mDrawable.setAlpha(i);
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (mDrawable != null) {
            mDrawable.setColorFilter(colorFilter);
        }
    }

    @Override
    public int getOpacity() {
        if (mDrawable != null) {
            return mDrawable.getOpacity();
        } else {
            return PixelFormat.UNKNOWN;
        }
    }

    @Override
    public RemoteAvatarDownloader getTarget() {
        return mRemoteImageToDrawable;
    }

    private void loadRemotely(Context context) {
        if (context == null) {
            return;
        }
        String url = GravatarFetcher.generateGravatarUrl(mEmail);
        if (url == null) {
            return;
        }
        mRemoteImageToDrawable.download(context, url, mDrawable);
    }
}
