package com.dashlane.autofill.api.util

import android.content.Context
import com.google.android.gms.auth.api.phone.SmsCodeAutofillClient
import com.google.android.gms.auth.api.phone.SmsCodeRetriever
import com.google.android.gms.tasks.Tasks
import kotlin.coroutines.suspendCoroutine

object SmsOtpPossibleChecker {

    suspend fun isSmsOtpAutofillPossible(context: Context, packageName: String): Boolean {
        val autofillClient = SmsCodeRetriever.getAutofillClient(context.applicationContext)
        val checkTask = autofillClient.hasOngoingSmsRequest(packageName)

        return suspendCoroutine { continuation ->

            checkTask.continueWithTask { checkRequestTask ->
                if (checkRequestTask.result == true) {
                    
                    
                    
                    Tasks.forCanceled()
                } else {
                    
                    
                    
                    
                    
                    autofillClient.checkPermissionState()
                }
            }.continueWithTask { checkPermissionTask ->
                if (checkPermissionTask.isCanceled) {
                    
                    
                    Tasks.forResult(false)
                } else {
                    
                    
                    
                    
                    Tasks.forResult(checkPermissionTask.result != SmsCodeAutofillClient.PermissionState.DENIED)
                }
            }.addOnSuccessListener { showSuggestion ->
                continuation.resumeWith(Result.success(showSuggestion))
            }.addOnFailureListener {
                
                
                
                
                continuation.resumeWith(Result.success(false))
            }
        }
    }
}