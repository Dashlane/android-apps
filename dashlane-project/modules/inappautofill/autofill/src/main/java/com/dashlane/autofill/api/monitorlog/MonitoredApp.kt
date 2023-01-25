package com.dashlane.autofill.api.monitorlog



interface MonitoredApp {
    

    enum class Browser : MonitoredApp {
        CHROME,
        SAMSUNG_BROWSER
    }

    

    enum class Keyboard : MonitoredApp {
        GBOARD,
        HONEYBOARD
    }
}