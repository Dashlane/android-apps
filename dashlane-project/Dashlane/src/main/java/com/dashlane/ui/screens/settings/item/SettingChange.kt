package com.dashlane.ui.screens.settings.item



interface SettingChange {

    interface Listenable {
        var listener: Listener?
    }

    interface Listener {
        fun onSettingsInvalidate()
    }
}