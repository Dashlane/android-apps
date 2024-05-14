package com.dashlane.ui.fab

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.dashlane.R
import com.dashlane.design.component.compat.view.BadgeView
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun View?.configureAsFab(
    @StringRes titleRes: Int,
    @StringRes titleDescription: Int,
    @DrawableRes drawableRes: Int,
    hasUpgradeBadge: Boolean = false
) {
    this?.findViewById<TextView>(R.id.title)?.run {
        setText(titleRes)
        contentDescription = context.getString(titleDescription)
    }
    this?.findViewById<FloatingActionButton>(R.id.icon)?.run {
        setImageResource(drawableRes)
    }
    this?.findViewById<BadgeView>(R.id.badge)?.run {
        visibility = if (hasUpgradeBadge) View.VISIBLE else View.GONE
    }
}

object FabViewUtil {
    interface LastMenuItemHiddenCallBack {
        fun onLastMenuItemHidden()
    }

    private const val ANIMATION_ADD_ICON_ROTATION_ANGLE_DEFAULT = 0f
    private const val ANIMATION_ADD_ICON_ROTATION_ANGLE = 135.0f
    private const val ANIMATION_BACKGROUND_ALPHA_INVISIBLE = 0f
    private const val ANIMATION_BACKGROUND_ALPHA_VISIBLE = 1f
    private const val ANIMATION_DURATION_DEFAULT = 100

    fun showFabMenu(
        fabMenuHolder: FrameLayout,
        fabButton: View,
        animate: Boolean
    ) {
        fabButton.isEnabled = false
        if (animate) {
            animateFabMenuShown(fabButton)
            animateMenuShown(fabMenuHolder)
        } else {
            fabButton.rotation = ANIMATION_ADD_ICON_ROTATION_ANGLE
            fabMenuHolder.alpha = ANIMATION_BACKGROUND_ALPHA_VISIBLE
            fabButton.isEnabled = true
        }
        fabButton.isSelected = true
        fabMenuHolder.visibility = View.VISIBLE
        showFabMenuItems(fabMenuHolder, fabButton, animate)
    }

    private fun animateFabMenuShown(fabButton: View) {
        val animator = fabButton.animate()
        animator.cancel()
        animator.rotation(ANIMATION_ADD_ICON_ROTATION_ANGLE)
            .setInterpolator(LinearInterpolator()).duration = ANIMATION_DURATION_DEFAULT.toLong()
        animator.setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                
            }

            override fun onAnimationEnd(animation: Animator) {
                fabButton.apply {
                    isEnabled = true
                    rotation = ANIMATION_ADD_ICON_ROTATION_ANGLE
                }
            }

            override fun onAnimationCancel(animation: Animator) {
                fabButton.apply {
                    isEnabled = true
                    rotation = ANIMATION_ADD_ICON_ROTATION_ANGLE_DEFAULT
                }
            }

            override fun onAnimationRepeat(animation: Animator) {
                
            }
        })
        animator.start()
    }

    fun animateMenuShown(fabMenuHolder: FrameLayout) {
        fabMenuHolder.alpha = ANIMATION_BACKGROUND_ALPHA_INVISIBLE
        val objectAnimator = ObjectAnimator.ofFloat(
            fabMenuHolder,
            "alpha",
            ANIMATION_BACKGROUND_ALPHA_INVISIBLE,
            ANIMATION_BACKGROUND_ALPHA_VISIBLE
        ).setDuration(
            (2 * ANIMATION_DURATION_DEFAULT).toLong()
        )
        objectAnimator.interpolator = AccelerateDecelerateInterpolator()
        objectAnimator.startDelay = ANIMATION_DURATION_DEFAULT.toLong()
        objectAnimator.start()
    }

    fun showFabMenuItems(
        fabMenuHolder: FrameLayout,
        fabButton: View,
        animate: Boolean,
        hideFabMenu: View.OnClickListener = View.OnClickListener { hideFabMenu(fabMenuHolder, fabButton, true) }
    ) {
        val menuHolder: ViewGroup = fabMenuHolder.findViewById(R.id.fab_menu_items_holder) ?: return
        menuHolder.setOnClickListener(hideFabMenu)
        var child: View
        for (i in menuHolder.childCount - 1 downTo 1) {
            child = menuHolder.getChildAt(i)
            child.visibility = View.VISIBLE
            val title = child.findViewById<View>(R.id.title)
            val icon = child.findViewById<View>(R.id.icon)
            if (animate) {
                val anim = AnimationUtils.loadAnimation(fabMenuHolder.context, R.anim.grow_from_bottom)
                anim.duration = ANIMATION_DURATION_DEFAULT.toLong()
                anim.startOffset =
                    ((menuHolder.childCount - (i + 1)) * (ANIMATION_DURATION_DEFAULT / 2) + ANIMATION_DURATION_DEFAULT).toLong()
                if (i == menuHolder.childCount - 1) {
                    anim.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation) {
                            scrollFabItemsToBottom(fabMenuHolder)
                        }

                        override fun onAnimationEnd(animation: Animation) {
                            
                        }

                        override fun onAnimationRepeat(animation: Animation) {
                            
                        }
                    })
                }
                title.startAnimation(anim)
                icon.startAnimation(anim)
            } else {
                if (i == 1) {
                    scrollFabItemsToBottom(fabMenuHolder)
                }
            }
        }
    }

    fun hideFabMenu(fabMenuHolder: FrameLayout, fabButton: View, animate: Boolean) {
        fabButton.isEnabled = false
        hideFabMenuItems(fabMenuHolder, animate, null)
        fabButton.isSelected = false
        if (animate) {
            animateFabMenuHidden(fabButton)
            animateMenuStateHidden(fabMenuHolder)
        } else {
            fabButton.rotation = ANIMATION_ADD_ICON_ROTATION_ANGLE_DEFAULT
            fabMenuHolder.visibility = View.GONE
            fabButton.isEnabled = true
        }
    }

    private fun animateFabMenuHidden(fabButton: View) {
        val animator = fabButton.animate()
        animator.cancel()
        animator.setListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                
            }

            override fun onAnimationEnd(animation: Animator) {
                fabButton.apply {
                    isEnabled = true
                    rotation = ANIMATION_ADD_ICON_ROTATION_ANGLE_DEFAULT
                }
            }

            override fun onAnimationCancel(animation: Animator) {
                fabButton.apply {
                    isEnabled = true
                    rotation = ANIMATION_ADD_ICON_ROTATION_ANGLE
                }
            }

            override fun onAnimationRepeat(animation: Animator) {
                
            }
        })
        animator.rotation(ANIMATION_ADD_ICON_ROTATION_ANGLE_DEFAULT)
            .setInterpolator(LinearInterpolator())
            .setStartDelay(ANIMATION_DURATION_DEFAULT.toLong())
            .setDuration(ANIMATION_DURATION_DEFAULT.toLong())
            .start()
    }

    fun animateMenuStateHidden(fabMenuHolder: FrameLayout) {
        val objectAnimator = ObjectAnimator.ofFloat(
            fabMenuHolder,
            "alpha",
            ANIMATION_BACKGROUND_ALPHA_VISIBLE,
            ANIMATION_BACKGROUND_ALPHA_INVISIBLE
        )
            .setDuration(2 * ANIMATION_DURATION_DEFAULT.toLong())
        objectAnimator.startDelay = ANIMATION_DURATION_DEFAULT.toLong()
        objectAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                
            }

            override fun onAnimationEnd(animation: Animator) {
                fabMenuHolder.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {
                
            }

            override fun onAnimationRepeat(animation: Animator) {
                
            }
        })
        objectAnimator.start()
    }

    fun hideFabMenuItems(
        fabMenuHolder: FrameLayout,
        animate: Boolean,
        lastMenuItemHiddenCallBack: LastMenuItemHiddenCallBack?
    ) {
        val menuHolder: ViewGroup = fabMenuHolder.findViewById(R.id.fab_menu_items_holder) ?: return
        for (i in menuHolder.childCount - 1 downTo 1) {
            val child = menuHolder.getChildAt(i)
            val title = child.findViewById<View>(R.id.title)
            val icon = child.findViewById<View>(R.id.icon)
            val count = menuHolder.childCount
            if (animate) {
                val anim =
                    AnimationUtils.loadAnimation(fabMenuHolder.context, R.anim.shrink_from_top)
                anim.startOffset = i * (ANIMATION_DURATION_DEFAULT / 2).toLong()
                anim.duration = ANIMATION_DURATION_DEFAULT.toLong()
                anim.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        
                    }

                    override fun onAnimationEnd(animation: Animation) {
                        child.visibility = View.INVISIBLE
                        if (i == count - 1 && lastMenuItemHiddenCallBack != null) {
                            child.post {
                                
                                lastMenuItemHiddenCallBack.onLastMenuItemHidden()
                            }
                        }
                    }

                    override fun onAnimationRepeat(animation: Animation) {
                        
                    }
                })
                title.startAnimation(anim)
                icon.startAnimation(anim)
            } else {
                child.visibility = View.INVISIBLE
            }
        }
    }

    private fun scrollFabItemsToBottom(fabMenuHolder: FrameLayout) {
        val scrollView: ScrollView? = fabMenuHolder.findViewById(R.id.fab_menu_items_scroller)
        scrollView?.fullScroll(View.FOCUS_DOWN)
    }
}
