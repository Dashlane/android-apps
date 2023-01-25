package com.dashlane.ui.model

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

sealed class TextResource {

    data class StringText(
        @StringRes val stringRes: Int,
        val args: List<Arg> = emptyList()
    ) : TextResource() {
        constructor(@StringRes stringRes: Int, arg: Arg) :
                this(stringRes, listOf(arg))

        constructor(@StringRes stringRes: Int, arg1: Arg, arg2: Arg) :
                this(stringRes, listOf(arg1, arg2))
    }

    data class PluralsText(
        @PluralsRes val pluralsRes: Int,
        val quantity: Int,
        val args: List<Arg> = emptyList()
    ) : TextResource() {
        constructor(@PluralsRes pluralsRes: Int, quantity: Int, arg: Arg) :
                this(pluralsRes, quantity, listOf(arg))

        constructor(@PluralsRes pluralsRes: Int, quantity: Int, arg1: Arg, arg2: Arg) :
                this(pluralsRes, quantity, listOf(arg1, arg2))
    }

    sealed class Arg {
        data class StringArg(val arg: String) : Arg()
        data class StringResArg(@StringRes val arg: Int) : Arg()
        data class IntArg(val arg: Int) : Arg()
    }

    @Suppress("SpreadOperator")
    fun format(resources: Resources): String {
        return when (this) {
            is StringText -> resources.getString(
                stringRes,
                *args.map { it.format(resources) }.toTypedArray()
            )
            is PluralsText -> resources.getQuantityString(
                pluralsRes,
                quantity,
                *args.map { it.format(resources) }.toTypedArray()
            )
        }
    }

    private fun Arg.format(resources: Resources): Any = when (this) {
        is Arg.StringArg -> arg
        is Arg.StringResArg -> resources.getString(arg)
        is Arg.IntArg -> arg
    }
}