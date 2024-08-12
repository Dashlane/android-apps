package com.dashlane.collections.sharing

import android.app.Activity
import android.content.Intent
import com.dashlane.R
import com.dashlane.collections.sharing.share.CollectionNewShareActivity.Companion.RESULT_EXTRA_COLLECTION_ID
import com.dashlane.collections.sharing.share.CollectionNewShareActivity.Companion.RESULT_EXTRA_COLLECTION_NAME
import com.dashlane.collections.sharing.share.CollectionNewShareActivity.Companion.RESULT_EXTRA_IS_BUSINESS
import com.dashlane.collections.sharing.share.CollectionNewShareActivity.Companion.RESULT_EXTRA_IS_ERROR
import com.dashlane.collections.sharing.share.CollectionNewShareActivity.Companion.SHARE_COLLECTION
import com.dashlane.navigation.Navigator
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.util.SnackbarUtils
import java.lang.ref.WeakReference
import javax.inject.Inject

class CollectionSharingResultActivityListener @Inject constructor(
    private val navigator: Navigator
) : AbstractActivityLifecycleListener() {

    private lateinit var activity: WeakReference<DashlaneActivity>

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        if (activity !is HomeActivity) return
        this.activity = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        super.onActivityPaused(activity)
        if (activity !is HomeActivity) return
        this.activity.clear()
    }

    override fun onActivityResult(
        activity: DashlaneActivity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(activity, requestCode, resultCode, data)
        if (requestCode != SHARE_COLLECTION) return
        val isError = resultCode == Activity.RESULT_CANCELED &&
            data?.getBooleanExtra(RESULT_EXTRA_IS_ERROR, false) == true
        val collectionName = data?.getStringExtra(RESULT_EXTRA_COLLECTION_NAME)
            ?: activity.getString(R.string.collection_new_share_snackbar_default_collection_name)
        val message = if (isError) {
            activity.getString(R.string.collection_new_share_error_snackbar, collectionName)
        } else {
            activity.getString(R.string.collection_new_share_success_snackbar, collectionName)
        }
        
        
        if (!isError && resultCode == Activity.RESULT_OK) {
            val collectionId = data?.getStringExtra(RESULT_EXTRA_COLLECTION_ID)!!
            val isBusiness = data.getBooleanExtra(RESULT_EXTRA_IS_BUSINESS, false)
            
            navigator.goToCollectionDetails(
                collectionId = collectionId,
                businessSpace = isBusiness,
                sharedCollection = true,
                shareAllowed = true
            )
        }
        
        if (isError || resultCode == Activity.RESULT_OK) {
            SnackbarUtils.showSnackbar(activity, message)
        }
    }
}