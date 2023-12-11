package com.dashlane.util.graphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.dashlane.url.icon.UrlDomainIcon
import com.dashlane.url.icon.UrlDomainIconAndroidRepository
import com.dashlane.url.toUrlDomainOrNull
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

open class CredentialRemoteDrawable(
    private val context: Context,
    backgroundColor: Int
) : RoundRectDrawable(context, backgroundColor) {
    private var placeholder: Drawable? = null
    val target: ImageDownloadedTarget = ImageDownloadedTarget(this)

    fun loadImage(url: String?, placeholder: Drawable?) {
        this.placeholder = placeholder
        
        setPlaceholder()
        if (url != null) {
            val urlDomain = url.toUrlDomainOrNull()
            if (urlDomain != null) {
                val urlDomainIconRepository =
                    EntryPointAccessors.fromApplication<RemoteImageEntryPoint>(context).urlDomainIconAndroidRepository
                val flow = urlDomainIconRepository[urlDomain]
                context.launchCollect(flow, DrawableIconCallback(this))
            }
        }
    }

    private fun loadImage(url: String?, backgroundColor: Int) {
        
        if (placeholder is BackgroundColorDrawable) {
            (placeholder as BackgroundColorDrawable).backgroundColor = backgroundColor
        }
        if (url == null) {
            setPlaceholder()
        } else {
            Glide.with(context)
                .load(url)
                .placeholder(placeholder)
                .into(target)
        }
    }

    private fun setPlaceholder() {
        Glide.with(context).clear(target)
        image = placeholder
    }

    protected fun drawBottomRightIcon(canvas: Canvas, bounds: Rect, drawable: Drawable) {
        
        val iconSize = (bounds.height() * 0.35f).roundToInt()
        val rect = Rect(bounds.right - iconSize, bounds.bottom - iconSize, bounds.right, bounds.bottom)
        val cornerRadius = (bounds.height() * ROUND_CORNER_SIZE_RATIO).roundToInt()
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = -0x19bdbbbc

        
        val path = Path()
        val radii = floatArrayOf(
            cornerRadius.toFloat(),
            cornerRadius.toFloat(),
            0f,
            0f,
            cornerRadius.toFloat(),
            cornerRadius.toFloat(),
            0f,
            0f
        )
        path.addRoundRect(RectF(rect), radii, Path.Direction.CW)
        canvas.drawPath(path, paint)

        
        drawable.setBounds(bounds.right - iconSize, bounds.bottom - iconSize, bounds.right, bounds.bottom)
        drawable.draw(canvas)
    }

    class ImageDownloadedTarget(
        private val roundRectDrawable: CredentialRemoteDrawable
    ) : CustomTarget<Drawable?>() {
        override fun onLoadStarted(placeholder: Drawable?) {
            if (placeholder != null) {
                roundRectDrawable.image = placeholder
            }
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            
        }

        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {
            roundRectDrawable.image = resource
        }
    }

    private class DrawableIconCallback(drawable: CredentialRemoteDrawable) : Function1<UrlDomainIcon?, Unit> {
        private val weakReference = WeakReference(drawable)

        override fun invoke(urlDomainIcon: UrlDomainIcon?) {
            val url = urlDomainIcon?.url ?: return
            val backgroundColor = urlDomainIcon.backgroundColor ?: return
            loadImage(url, backgroundColor)
        }

        private fun loadImage(url: String, @ColorInt backgroundColor: Int) {
            val drawable = weakReference.get() ?: return
            
            try {
                drawable.loadImage(url, backgroundColor)
            } catch (e: Exception) {
                warn("Error while loading image", "", e)
            }
        }
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface RemoteImageEntryPoint {
        val urlDomainIconAndroidRepository: UrlDomainIconAndroidRepository
    }
}