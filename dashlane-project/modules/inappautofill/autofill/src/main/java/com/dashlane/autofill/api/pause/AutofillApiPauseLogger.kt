package com.dashlane.autofill.api.pause



interface AutofillApiPauseLogger {

    

    fun onClickPauseSuggestion(packageName: String, webappDomain: String, hasCredentials: Boolean, loggedIn: Boolean)

    

    fun onClickShortPause(packageName: String, webappDomain: String, hasCredentials: Boolean, loggedIn: Boolean)

    

    fun onClickLongPause(packageName: String, webappDomain: String, hasCredentials: Boolean, loggedIn: Boolean)

    

    fun onClickDefinitePause(packageName: String, webappDomain: String, hasCredentials: Boolean, loggedIn: Boolean)
}