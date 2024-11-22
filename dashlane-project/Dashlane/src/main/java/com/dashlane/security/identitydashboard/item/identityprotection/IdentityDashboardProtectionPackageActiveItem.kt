package com.dashlane.security.identitydashboard.item.identityprotection

import android.content.Context
import android.text.style.BulletSpan
import android.view.View
import android.widget.TextView
import com.dashlane.R
import com.dashlane.security.identitydashboard.item.IdentityDashboardItem
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.util.dpToPx
import com.dashlane.util.getThemeAttrColor
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder
import kotlin.math.roundToInt

class IdentityDashboardProtectionPackageActiveItem(
    val listener: ActiveProtectionPackageListener
) : IdentityDashboardItem {

    override fun getViewType() = VIEW_TYPE

    class ViewHolder(val item: View) : EfficientViewHolder<IdentityDashboardProtectionPackageActiveItem>(item) {
        init {
            findViewByIdEfficient<View>(R.id.protection_button)?.setOnClickListener {
                `object`?.listener?.onActiveProtectionLearnMoreClick()
            }
            findViewByIdEfficient<View>(R.id.restoration_button)?.setOnClickListener {
                `object`?.listener?.onActiveRestorationLearnMoreClick()
            }
        }

        override fun updateView(context: Context, item: IdentityDashboardProtectionPackageActiveItem?) {
            item ?: return
            val bulletSpan = getBulletSpan(context)
            setCreditIdentityProtectionSection(bulletSpan)
            setIdentityRestorationSection(bulletSpan)
        }

        private fun setCreditIdentityProtectionSection(bulletSpan: BulletSpan) {
            val phoneNumber = context.getString(R.string.identity_protection_phone_number)

            
            findViewByIdEfficient<TextView>(R.id.protection_description_1)?.text =
                context.getSpannableStringBuilder(R.string.identity_protection_active_description_1, phoneNumber)
                    .toBulletPointSpannable(bulletSpan)

            
            findViewByIdEfficient<TextView>(R.id.protection_description_2)?.text =
                context.getSpannableStringBuilder(R.string.identity_protection_active_description_2)
                    .toBulletPointSpannable(bulletSpan)

            
            findViewByIdEfficient<TextView>(R.id.protection_description_3)?.text =
                context.getSpannableStringBuilder(R.string.identity_protection_active_description_3)
                    .toBulletPointSpannable(bulletSpan)
        }

        private fun setIdentityRestorationSection(bulletSpan: BulletSpan) {
            val phoneNumber = context.getString(R.string.identity_restoration_phone_number)
            val dashlaneCode = context.getString(R.string.identity_restoration_dashlane_code)

            
            findViewByIdEfficient<TextView>(R.id.restoration_description_1)?.text =
                context.getSpannableStringBuilder(
                    R.string.identity_restoration_active_description_1,
                    phoneNumber,
                    dashlaneCode
                )
                    .toBulletPointSpannable(bulletSpan)

            
            findViewByIdEfficient<TextView>(R.id.restoration_description_2)?.text =
                context.getSpannableStringBuilder(R.string.identity_restoration_active_description_2)
                    .toBulletPointSpannable(bulletSpan)

            
            findViewByIdEfficient<TextView>(R.id.restoration_description_3)?.text =
                context.getSpannableStringBuilder(R.string.identity_restoration_active_description_3)
                    .toBulletPointSpannable(bulletSpan)
        }

        private fun getBulletSpan(context: Context): BulletSpan {
            val marginBullet = context.resources.dpToPx(BULLET_GAP_BASE_WIDTH).roundToInt()
            return BulletSpan(
                marginBullet,
                context.getThemeAttrColor(R.attr.colorOnBackground),
                context.resources.dpToPx(BULLET_BASE_WIDTH).roundToInt()
            )
        }

        companion object {
            const val BULLET_GAP_BASE_WIDTH = 8F
            const val BULLET_BASE_WIDTH = 2F
        }
    }

    interface ActiveProtectionPackageListener {
        fun onActiveProtectionLearnMoreClick()
        fun onActiveRestorationLearnMoreClick()
    }

    companion object {
        val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            R.layout.item_id_protection_package_active,
            ViewHolder::class.java
        )
    }
}