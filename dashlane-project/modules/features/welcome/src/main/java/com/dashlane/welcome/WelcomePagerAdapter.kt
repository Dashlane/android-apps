package com.dashlane.welcome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

internal class WelcomePagerAdapter : PagerAdapter(), ViewPager.OnPageChangeListener {
    private val items = getItems()

    private val positionProgressListeners = (0 until count).map { mutableListOf<(progress: Float) -> Unit>() }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return LayoutInflater.from(container.context)
            .inflate(R.layout.item_welcome, container, false)
            .apply {
                val item = items[position]

                findViewById<View>(R.id.animation_frame).run {
                    positionProgressListeners[position].add { alpha = 0.25f + 1.5f * if (it > 0.5) 1f - it else it }
                }
                findViewById<LottieAnimationView>(R.id.main_animation)
                    .setAnimation(position, item.mainAnimation)
                findViewById<LottieAnimationView>(R.id.background_animation)
                    .setAnimation(position, item.backgroundAnimation)
                findViewById<TextView>(R.id.title).setText(item.title)
                findViewById<TextView>(R.id.description).setText(item.description)

                container.addView(this)
            }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        positionProgressListeners[position].clear()
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any) = view === `object`

    override fun getCount() = items.size

    override fun onPageScrollStateChanged(state: Int) = Unit

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        
        val leftPageProgress = 0.5f + positionOffset.coerceIn(0f, 0.5f)
        positionProgressListeners[position].forEach { it(leftPageProgress) }

        if (position + 1 == count) return

        
        val rightPageProgress = positionOffset.coerceIn(0.5f, 1f) - 0.5f
        positionProgressListeners[position + 1].forEach { it(rightPageProgress) }
    }

    override fun onPageSelected(position: Int) = Unit

    private fun LottieAnimationView.setAnimation(position: Int, desc: AnimationDesc?) {
        if (desc == null) return

        setAnimation(desc.res)

        when (desc) {
            is AnimationDesc.Loop -> {
                repeatCount = LottieDrawable.INFINITE
                positionProgressListeners[position].add { if (it == 0.5f) resumeAnimation() else pauseAnimation() }
            }
            is AnimationDesc.Progress -> {
                positionProgressListeners[position].add {
                    progress = if (it == 0.5f && desc.midMarker != null) {
                        setMinFrame(desc.midMarker)
                        0f
                    } else {
                        setMinFrame(0)
                        it
                    }
                }
            }
        }
    }

    private data class Item(
        @StringRes val title: Int,
        @StringRes val description: Int,
        val mainAnimation: AnimationDesc,
        val backgroundAnimation: AnimationDesc? = null
    )

    private sealed class AnimationDesc {
        @get:RawRes
        abstract val res: Int

        class Loop(
            @RawRes override val res: Int
        ) : AnimationDesc()

        class Progress(
            @RawRes override val res: Int,
            val midMarker: String? = null
        ) : AnimationDesc()
    }

    companion object {
        @Suppress("SpreadOperator")
        private fun getItems() = listOfNotNull(
            Item(
                title = R.string.welcome_trust_title,
                description = R.string.welcome_trust_description,
                mainAnimation = AnimationDesc.Loop(
                    res = R.raw.lottie_welcome_trust
                ),
                backgroundAnimation = AnimationDesc.Progress(
                    res = R.raw.lottie_welcome_trust_background
                )
            ),
            Item(
                title = R.string.welcome_vault_title,
                description = R.string.welcome_vault_description,
                mainAnimation = AnimationDesc.Progress(
                    res = R.raw.lottie_welcome_vault,
                    midMarker = "stop"
                ),
                backgroundAnimation = AnimationDesc.Loop(
                    res = R.raw.lottie_welcome_vault_background
                )
            ),
            Item(
                title = R.string.welcome_autofill_title,
                description = R.string.welcome_autofill_description,
                mainAnimation = AnimationDesc.Loop(
                    res = R.raw.lottie_welcome_autofill
                )
            ),
            Item(
                title = R.string.welcome_alert_title,
                description = R.string.welcome_alert_description,
                mainAnimation = AnimationDesc.Loop(
                    res = R.raw.lottie_welcome_alert
                )
            ),
            Item(
                title = R.string.welcome_encryption_title,
                description = R.string.welcome_encryption_description,
                mainAnimation = AnimationDesc.Progress(
                    res = R.raw.lottie_welcome_encryption
                )
            )
        )
    }
}