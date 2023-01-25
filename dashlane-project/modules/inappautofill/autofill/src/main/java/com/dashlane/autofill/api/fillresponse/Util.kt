package com.dashlane.autofill.api.fillresponse

import android.os.Build
import android.widget.inline.InlinePresentationSpec

internal fun InlinePresentationSpec?.isAvailable() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && this != null