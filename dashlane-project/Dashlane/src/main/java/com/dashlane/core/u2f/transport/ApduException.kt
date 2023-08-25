package com.dashlane.core.u2f.transport

class ApduException(val code: Int) : Exception(String.format("APDU status: %04x", code))