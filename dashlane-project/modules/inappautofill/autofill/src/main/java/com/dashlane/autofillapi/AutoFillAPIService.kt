package com.dashlane.autofillapi

import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveRequest
import android.service.autofill.SavedDatasetsInfo
import android.service.autofill.SavedDatasetsInfoCallback
import android.view.autofill.AutofillId
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import androidx.autofill.inline.UiVersions
import com.dashlane.autofill.AutoFillBlackListImpl
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.accessibility.AccessibilityApiServiceDetector
import com.dashlane.autofill.accessibility.AccessibilityApiServiceDetectorImpl
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.dagger.AutofillApiInternalComponent
import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.autofill.api.internal.AutofillLimiter
import com.dashlane.autofill.api.request.autofill.logger.getAutofillApiOrigin
import com.dashlane.autofill.api.request.save.SaveRequestActivity
import com.dashlane.autofill.api.util.AutofillValueFactoryAndroidImpl
import com.dashlane.autofill.formdetector.AutoFillHintsExtractor
import com.dashlane.autofill.formdetector.AutofillPackageNameAcceptor
import com.dashlane.autofill.formdetector.BrowserDetectionHelper
import com.dashlane.performancelogger.TimeToAutofillLogger
import com.dashlane.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext



class AutoFillAPIService : AutofillService(), CoroutineScope {

    private val component: AutofillApiComponent by lazy(LazyThreadSafetyMode.NONE) {
        AutofillApiComponent(this)
    }

    private val componentInternal: AutofillApiInternalComponent by lazy(LazyThreadSafetyMode.NONE) {
        AutofillApiInternalComponent(this)
    }

    private val autoFillApiUsageLog: AutofillAnalyzerDef.IAutofillUsageLog
        get() = component.autoFillApiUsageLog

    private val sessionManager: SessionManager
        get() = component.sessionManager

    private val autofillLimiter: AutofillLimiter
        get() = component.autofillLimiter

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job

    private lateinit var accessibilityApiServiceDetector: AccessibilityApiServiceDetector

    override fun onCreate() {
        setTheme(R.style.Theme_Dashlane)
        super.onCreate()
        accessibilityApiServiceDetector =
            AccessibilityApiServiceDetectorImpl(this, BrowserDetectionHelper)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun FillRequest.getFocusAutofillId(): AutofillId? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.fillContexts.filter {
                it.requestId == this.id
            }.map {
                it.focusedId
            }.lastOrNull()
        } else {
            null
        }
    }

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val timeToAutofillLogger = component.timeToAutofillLogger
        timeToAutofillLogger.logStart()

        val inlineSpecs: List<InlinePresentationSpec>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && canUseInlineAutofill(request)) {
                request.inlineSuggestionsRequest!!.inlinePresentationSpecs
            } else {
                null
            }
        logIfManualRequest(request, inlineSpecs?.isNotEmpty() ?: false)

        val mBlacklistPackages = AutoFillBlackListImpl()
        val autofillValueFactory = AutofillValueFactoryAndroidImpl()
        val packageNameAcceptor = AutofillPackageNameAcceptor(
            mBlacklistPackages,
            accessibilityApiServiceDetector
        )
        val autoFillHintsExtractor = AutoFillHintsExtractor(autofillValueFactory, packageNameAcceptor)

        launch(Main) {
            try {
                handleFillRequest(
                    request,
                    autoFillHintsExtractor,
                    callback,
                    inlineSpecs,
                    timeToAutofillLogger,
                    request.getFocusAutofillId()
                )
            } catch (e: Exception) {
                respondSuccess(callback, timeToAutofillLogger = timeToAutofillLogger)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun canUseInlineAutofill(request: FillRequest): Boolean {
        val style =
            request.inlineSuggestionsRequest?.inlinePresentationSpecs?.get(0)?.style ?: return false

        
        return hasInlineAutofillEnabled() && UiVersions.getVersions(style)
            .contains(UiVersions.INLINE_UI_VERSION_1)
    }

    private fun hasInlineAutofillEnabled() =
        component.userPreferencesAccess.hasKeyboardAutofillEnabled()

    private fun respondSuccess(
        callback: FillCallback,
        response: FillResponse? = null,
        timeToAutofillLogger: TimeToAutofillLogger
    ) {
        component.timeToLoadLocalLogger.logStop()
        timeToAutofillLogger.logStop()
        runCatching {
            callback.onSuccess(response)
        }
    }

    private suspend fun handleFillRequest(
        request: FillRequest,
        autoFillHintsExtractor: AutoFillHintsExtractor,
        callback: FillCallback,
        inlineSpecs: List<InlinePresentationSpec>?,
        timeToAutofillLogger: TimeToAutofillLogger,
        focusedAutofillId: AutofillId? = null
    ) {
        
        val summary = autoFillHintsExtractor.extractOnlyRequested(request)

        
        if (!request.isManualRequest() &&
            summary?.formSource?.let { autofillLimiter.canHandle(it) } == true
        ) {
            callback.onSuccess(null)
            return
        }

        val fillResponse = summary?.let {
            componentInternal.fillRequestHandler.getFillResponse(
                this@AutoFillAPIService,
                request.clientState,
                summary,
                inlineSpecs,
                timeToAutofillLogger,
                focusedAutofillId
            )
        }

        respondSuccess(callback, fillResponse, timeToAutofillLogger)
    }

    override fun onSaveRequest(
        request: SaveRequest,
        callback: android.service.autofill.SaveCallback
    ) {
        val mBlacklistPackages = AutoFillBlackListImpl()
        val autofillValueFactory = AutofillValueFactoryAndroidImpl()
        val packageNameAcceptor = AutofillPackageNameAcceptor(
            mBlacklistPackages,
            accessibilityApiServiceDetector
        )
        val autoFillHintsExtractor = AutoFillHintsExtractor(autofillValueFactory, packageNameAcceptor)

        
        val summary = autoFillHintsExtractor.extract(request)

        if (summary == null) {
            callback.onFailure(null)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            callback.onSuccess(SaveRequestActivity.getSaveRequestPendingIntent(this, summary, request))
        } else {
            SaveRequestActivity.getSaveRequestIntent(this, summary, request)
            
            callback.onSuccess()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onSavedDatasetsInfoRequest(callback: SavedDatasetsInfoCallback) {
        val numberOfPasswords: Int = component.databaseAccess.authentifiantCount
        if (numberOfPasswords != -1) {
            callback.onSuccess(
                setOf(
                    SavedDatasetsInfo(
                        SavedDatasetsInfo.TYPE_PASSWORDS,
                        numberOfPasswords
                    )
                )
            )
        } else {
            callback.onError(SavedDatasetsInfoCallback.ERROR_OTHER)
        }
    }

    private fun logIfManualRequest(
        request: FillRequest,
        forKeyboard: Boolean
    ) {
        
        if (sessionManager.session == null) return
        
        if (!request.isManualRequest()) return

        val packageName =
            request.fillContexts.lastOrNull()?.structure?.activityComponent?.packageName
                ?: return 

        autoFillApiUsageLog.onManualRequestAsked(getAutofillApiOrigin(forKeyboard), packageName)
    }

    private fun FillRequest.isManualRequest(): Boolean =
        (flags or FillRequest.FLAG_MANUAL_REQUEST) == flags
}