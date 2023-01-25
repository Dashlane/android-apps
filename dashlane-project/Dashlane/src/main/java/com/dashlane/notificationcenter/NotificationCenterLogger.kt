package com.dashlane.notificationcenter



interface NotificationCenterLogger {
    

    fun logActionItemCenterShow()

    

    fun logActionItemShow(key: String)

    

    fun logActionItemClick(key: String)

    

    fun logActionItemDismiss(key: String)

    

    fun logActionItemUndoDismiss(key: String)

    companion object {
        const val ORIGIN = "action_items"
    }

    interface OriginProvider {
        val origin: String
    }
}