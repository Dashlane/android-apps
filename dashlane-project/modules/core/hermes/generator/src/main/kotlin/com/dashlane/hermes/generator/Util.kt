package com.dashlane.hermes.generator

import java.net.URI
import java.util.Locale

val URI.className: String
    get() {
        
        
        return if (fragment == null) {
            path.split('/').last().substringBefore('.').snakeCaseToCamelCase().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
            }
        } else {
            fragment.split(
                '/',
            ).last().snakeCaseToCamelCase().replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(Locale.ROOT)
                } else {
                    it.toString()
                }
            }
        }
    }

fun String.snakeCaseToCamelCase() =
    replace(Regex("_([a-z])")) {
        it.groups[1]!!.value.uppercase(Locale.US)
    }

fun String.slashToSnakeCase() = replace("/", "_")