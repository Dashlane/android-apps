package com.dashlane.security.identitydashboard.breach



interface BreachLogger {
    

    fun logPopupShow(breachWrapper: BreachWrapper)

    

    fun logPopupView(breachWrapper: BreachWrapper)

    

    fun logPopupClose(breachWrapper: BreachWrapper)

    

    fun logMultiPopupShow(alertCount: String)

    

    fun logMultiPopupView(alertCount: String)

    

    fun logMultiPopupClose(alertCount: String)

    

    fun logTrayShow(itemPosition: Int, breachWrapper: BreachWrapper)

    

    fun logTrayClose(itemPosition: Int, breachWrapper: BreachWrapper)

    

    fun logOpenAlertDetail()
}