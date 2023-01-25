package com.dashlane.m2w

import android.app.Activity
import android.content.Intent
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.util.startActivityForResult
import com.skocken.presentation.presenter.BasePresenter

internal class M2wIntroPresenter : BasePresenter<IntroScreenContract.DataProvider, IntroScreenContract.ViewProxy>(),
    IntroScreenContract.Presenter {

    var logger: M2wIntroLogger? = null
    var origin: String? = null

    override fun onViewChanged() {
        super.onViewChanged()
        viewOrNull?.apply {
            setImageResourceWithTint(R.drawable.logo_m2w, null)
            setTitle(R.string.m2w_intro_title)
            setDescription(R.string.m2w_intro_body)
            setPositiveButton(R.string.m2w_intro_start)
            setNegativeButton(R.string.m2w_intro_skip)
        }
    }

    override fun onClickPositiveButton() {
        logger?.logNext()

        val origin = this.origin ?: return

        activity?.startActivityForResult<M2wConnectActivity>(M2wConnectActivity.REQUEST_CODE) {
            M2wIntentCoordinator.putConnectActivityExtras(this, origin)
        }
    }

    override fun onClickNegativeButton() {
        val intent = Intent().apply {
            putExtra(M2wIntroActivity.EXTRA_SKIP, true)
        }
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    
    
    

    override fun onClickNeutralButton() = Unit
    override fun onClickLink(position: Int, label: Int) = Unit
}