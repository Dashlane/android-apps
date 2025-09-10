package com.dashlane.autofill.formdetector.model

val AutoFillViewNode.children: Sequence<AutoFillViewNode>
    get() = object : Iterator<AutoFillViewNode?> {
        private var index = 0

        override fun hasNext() = index < childCount

        override fun next() =
            if (index < childCount) {
                getChild(index++)
            } else {
                throw NoSuchElementException()
            }
    }.asSequence().filterNotNull()