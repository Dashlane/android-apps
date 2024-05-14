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
import androidx.compose.ui.platform.AndroidUiDispatcher.Companion.Main
import com.dashlane.autofill.AutoFillBlackListImpl
import com.dashlane.autofill.AutofillAnalyzerDef
import com.dashlane.autofill.FillRequestHandler
import com.dashlane.autofill.accessibility.AccessibilityApiServiceDetector
import com.dashlane.autofill.accessibility.AccessibilityApiServiceDetectorImpl
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.util.AutofillValueFactoryAndroidImpl
import com.dashlane.autofill.formdetector.AutoFillHintsExtractor
import com.dashlane.autofill.formdetector.AutofillPackageNameAcceptor
import com.dashlane.autofill.formdetector.BrowserDetectionHelper
import com.dashlane.autofill.internal.AutofillLimiter
import com.dashlane.autofill.request.save.SaveRequestActivity
import com.dashlane.common.logger.developerinfo.DeveloperInfoLogger
import com.dashlane.util.stackTraceToSafeString
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AutoFillAPIService : AutofillService(), CoroutineScope {

    @Inject
    lateinit var databaseAccess: AutofillAnalyzerDef.DatabaseAccess

    @Inject
    lateinit var userPreferencesAccess: AutofillAnalyzerDef.IUserPreferencesAccess

    @Inject
    lateinit var fillRequestHandler: FillRequestHandler

    @Inject
    lateinit var autofillLimiter: AutofillLimiter

    @Inject
    lateinit var developerInfoLogger: DeveloperInfoLogger

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
        val inlineSpecs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && canUseInlineAutofill(request)) {
            request.inlineSuggestionsRequest!!.inlinePresentationSpecs
        } else {
            null
        }

        val mBlacklistPackages = AutoFillBlackListImpl()
        val autofillValueFactory = AutofillValueFactoryAndroidImpl()
        val packageNameAcceptor = AutofillPackageNameAcceptor(mBlacklistPackages, accessibilityApiServiceDetector)
        val autoFillHintsExtractor = AutoFillHintsExtractor(autofillValueFactory, packageNameAcceptor)

        launch(Main) {
            try {
                handleFillRequest(
                    request,
                    autoFillHintsExtractor,
                    callback,
                    inlineSpecs,
                    request.getFocusAutofillId()
                )
            } catch (e: Exception) {
                developerInfoLogger.log(
                    action = "autofill_error",
                    message = e.message,
                    exceptionType = e.stackTraceToSafeString()
                )
                respondSuccess(callback)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun canUseInlineAutofill(request: FillRequest): Boolean {
        val style = request.inlineSuggestionsRequest?.inlinePresentationSpecs?.get(0)?.style ?: return false

        
        return hasInlineAutofillEnabled() && UiVersions.getVersions(style)
            .contains(UiVersions.INLINE_UI_VERSION_1)
    }

    private fun hasInlineAutofillEnabled() =
        userPreferencesAccess.hasKeyboardAutofillEnabled()

    private fun respondSuccess(
        callback: FillCallback,
        response: FillResponse? = null
    ) {
        runCatching {
            callback.onSuccess(response)
        }
    }

    private suspend fun handleFillRequest(
        request: FillRequest,
        autoFillHintsExtractor: AutoFillHintsExtractor,
        callback: FillCallback,
        inlineSpecs: List<InlinePresentationSpec>?,
        focusedAutofillId: AutofillId? = null
    ) {
        
        val summary = autoFillHintsExtractor.extractOnlyRequested(request)

        
        if (!request.isManualRequest() &&
            summary?.formSource?.let { autofillLimiter.canHandle(it) } == true
        ) {
            callback.onSuccess(null)
            return
        }

        if (summary?.isRequestTimedOut == true) {
            developerInfoLogger.log(
                action = "autofill_timeout",
                message = "Autofill request timed out internally"
            )
        }

        val fillResponse = summary?.let {
            fillRequestHandler.getFillResponse(
                this@AutoFillAPIService,
                request.clientState,
                summary,
                inlineSpecs,
                focusedAutofillId
            )
        }

        respondSuccess(callback, fillResponse)
    }

    override fun onSaveRequest(
        request: SaveRequest,
        callback: android.service.autofill.SaveCallback
    ) {
        val mBlacklistPackages = AutoFillBlackListImpl()
        val autofillValueFactory = AutofillValueFactoryAndroidImpl()
        val packageNameAcceptor = AutofillPackageNameAcceptor(mBlacklistPackages, accessibilityApiServiceDetector)
        val autoFillHintsExtractor = AutoFillHintsExtractor(autofillValueFactory, packageNameAcceptor)

        launch(Main) {
            
            val summary = autoFillHintsExtractor.extract(request)

            if (summary == null) {
                callback.onFailure(null)
                return@launch
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                callback.onSuccess(
                    SaveRequestActivity.getSaveRequestPendingIntent(this@AutoFillAPIService, summary, request)
                )
            } else {
                SaveRequestActivity.getSaveRequestIntent(this@AutoFillAPIService, summary, request)
                
                callback.onSuccess()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onSavedDatasetsInfoRequest(callback: SavedDatasetsInfoCallback) {
        val numberOfPasswords: Int = databaseAccess.authentifiantCount
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

    private fun FillRequest.isManualRequest(): Boolean =
        (flags or FillRequest.FLAG_MANUAL_REQUEST) == flags
}