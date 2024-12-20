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
import com.dashlane.autofill.dagger.AutofillApiInternalEntryPoint
import com.dashlane.autofill.formdetector.AutoFillHintsExtractor
import com.dashlane.autofill.formdetector.AutofillPackageNameAcceptor
import com.dashlane.autofill.formdetector.BrowserDetectionHelper
import com.dashlane.autofill.internal.AutofillLimiter
import com.dashlane.autofill.request.save.SaveRequestActivity
import com.dashlane.preference.PreferencesManager
import com.dashlane.session.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EarlyEntryPoints
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AutoFillAPIService : AutofillService(), CoroutineScope {

    private val componentInternal: AutofillApiInternalEntryPoint
        get() = EarlyEntryPoints.get(applicationContext, AutofillApiInternalEntryPoint::class.java)

    private val databaseAccess: AutofillAnalyzerDef.DatabaseAccess
        get() = componentInternal.databaseAccess

    private val preferencesManager: PreferencesManager
        get() = componentInternal.preferencesManager

    private val fillRequestHandler: FillRequestHandler
        get() = componentInternal.fillRequestHandler

    private val autofillLimiter: AutofillLimiter
        get() = componentInternal.autofillLimiter

    private val sessionManager: SessionManager
        get() = componentInternal.sessionManager

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
        return this.fillContexts.filter {
            it.requestId == this.id
        }.map {
            it.focusedId
        }.lastOrNull()
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
        preferencesManager[sessionManager.session?.username].hasInlineAutofill

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

            callback.onSuccess(SaveRequestActivity.getSaveRequestPendingIntent(this@AutoFillAPIService, summary, request))
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