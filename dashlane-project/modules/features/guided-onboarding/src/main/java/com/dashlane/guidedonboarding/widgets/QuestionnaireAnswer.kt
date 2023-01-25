package com.dashlane.guidedonboarding.widgets

import com.dashlane.guidedonboarding.R

enum class QuestionnaireAnswer(val id: Int) {
    
    AUTOFILL(0),
    M2W(1),
    DWM(2),

    
    MEMORY(10),
    BROWSER(11), 
    PASSWORD_MANAGER(12), 
    CSV(13), 
    OTHER(14),
    DIGITAL_TOOL(20),

    
    ACCOUNT_EMAIL(100)
    ;

    val action
        get() = when (this) {
            MEMORY -> R.string.guided_onboarding_handle_memory_action
            BROWSER -> R.string.guided_onboarding_handle_browser_action
            PASSWORD_MANAGER -> R.string.guided_onboarding_handle_password_manager_action
            CSV -> R.string.guided_onboarding_handle_csv_action
            OTHER -> R.string.guided_onboarding_handle_other_action
            DIGITAL_TOOL -> R.string.guided_onboarding_handle_digital_tool_action
            else -> 0
        }

    val lottieRes
        get() = when (this) {
            AUTOFILL -> R.raw.guided_onboarding_lottie_01_autofill
            DWM -> R.raw.guided_onboarding_lottie_03_breach
            M2W -> R.raw.guided_onboarding_lottie_08_devices
            MEMORY -> R.raw.guided_onboarding_lottie_05_vault
            OTHER -> R.raw.guided_onboarding_lottie_08_devices
            DIGITAL_TOOL -> R.raw.guided_onboarding_lottie_07_pwimport
            else -> 0
        }

    companion object {
        const val KEY_GUIDED_PASSWORD_ONBOARDING_Q2_ANSWER = "guided_password_onboarding_q2_answer"
        const val KEY_GUIDED_ONBOARDING_DWM_USER_HAS_ALERTS = "guided_password_onboarding_dwm_user_has_alerts"

        @JvmStatic
        fun fromId(id: Int): QuestionnaireAnswer? = values().firstOrNull { it.id == id }
    }
}