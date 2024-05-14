package com.dashlane.collections.sharing.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import com.dashlane.R
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.notificator.Notificator
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CollectionNewShareActivity : DashlaneActivity() {
    @Inject
    lateinit var notificator: Notificator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                CollectionNewShareScreen(
                    userNavigationListener = object : UserNavigationListener {
                        override fun onCloseClicked() {
                            setResult(RESULT_CANCELED)
                            finish()
                        }

                        override fun onSharingFailed(collectionName: String?) {
                            setResult(
                                RESULT_CANCELED,
                                Intent().apply {
                                    putExtra(RESULT_EXTRA_IS_ERROR, true)
                                    collectionName?.let {
                                        putExtra(RESULT_EXTRA_COLLECTION_NAME, it)
                                    }
                                }
                            )
                            finish()
                        }

                        override fun onSharingRestricted() {
                            notificator.customErrorDialogMessage(
                                this@CollectionNewShareActivity,
                                getString(R.string.teamspace_restrictions_sharing_title),
                                getString(R.string.teamspace_restrictions_sharing_description),
                                true
                            )
                        }

                        override fun onSharingSucceed(
                            collectionName: String?,
                            collectionId: String,
                            isBusiness: Boolean
                        ) {
                            setResult(
                                RESULT_OK,
                                Intent().apply {
                                    collectionName?.let {
                                        putExtra(RESULT_EXTRA_COLLECTION_NAME, it)
                                    }
                                    putExtra(RESULT_EXTRA_COLLECTION_ID, collectionId)
                                    putExtra(RESULT_EXTRA_IS_BUSINESS, isBusiness)
                                }
                            )
                            finish()
                        }
                    }
                )
            }
        }
    }

    companion object {
        const val SHARE_COLLECTION = 7408
        const val RESULT_EXTRA_IS_ERROR = "sharing_result_error"
        const val RESULT_EXTRA_COLLECTION_NAME = "sharing_result_collection_name"
        const val RESULT_EXTRA_COLLECTION_ID = "sharing_result_collection_id"
        const val RESULT_EXTRA_IS_BUSINESS = "sharing_result_collection_business"
    }
}

internal interface UserNavigationListener {
    fun onCloseClicked()
    fun onSharingSucceed(collectionName: String?, collectionId: String, isBusiness: Boolean)
    fun onSharingFailed(collectionName: String?)
    fun onSharingRestricted()
}