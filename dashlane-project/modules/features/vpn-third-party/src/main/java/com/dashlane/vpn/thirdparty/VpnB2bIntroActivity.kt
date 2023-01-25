package com.dashlane.vpn.thirdparty

import android.os.Bundle
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.skocken.presentation.presenter.BasePresenter

class VpnB2bIntroActivity : DashlaneActivity() {
    private lateinit var presenter: Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        presenter = Presenter()
        presenter.setView(IntroScreenViewProxy(this))
    }

    private class Presenter : BasePresenter<IntroScreenContract.DataProvider, IntroScreenContract.ViewProxy>(),
        IntroScreenContract.Presenter {

        override fun onViewChanged() {
            super.onViewChanged()
            viewOrNull?.apply {
                this.setImageResource(R.drawable.vpn_intro_logo)
                setTitle(R.string.vpn_intro_title_b2b)
                setDescription(R.string.vpn_intro_body_b2b)
                setNegativeButton(R.string.vpn_intro_button_close_b2b)
            }
        }

        override fun onClickPositiveButton() {
            
        }

        override fun onClickNegativeButton() {
            activity?.finish()
        }

        override fun onClickNeutralButton() {
            
        }

        override fun onClickLink(position: Int, label: Int) {
            
        }
    }
}