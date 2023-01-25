package com.dashlane.vpn.thirdparty.activate

interface VpnThirdPartyActivateAccountErrorListener {
    companion object {
        const val ERROR_TYPE_UNKNOWN = 0
        const val ERROR_TYPE_ACCOUNT_EXISTS = 1
    }
    fun onError(errorType: Int)
    fun onTryAgain()
    fun onContactSupport()
}