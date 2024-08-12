package com.dashlane.autofill.phishing

import android.content.Context
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.runIfNull
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.File
import java.nio.FloatBuffer
import javax.inject.Inject
import kotlin.math.min

class UrlPhishingClassificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val antiPhishingFilesDownloader: AntiPhishingFilesDownloader
) {
    suspend fun classifyWebsitePhishingLevel(url: String): PhishingAttemptLevel {
        val denyList = antiPhishingFilesDownloader.getPhishingDenyList()
        val allowList = antiPhishingFilesDownloader.getPhishingAllowList()
        if (denyList?.domains?.any { url.toUrlDomainOrNull()?.root == it.toUrlDomainOrNull()?.root } == true) {
            return PhishingAttemptLevel.HIGH
        }
        if (allowList?.domains?.any { url.toUrlDomainOrNull()?.root == it.toUrlDomainOrNull()?.root } == true) {
            return PhishingAttemptLevel.NONE
        }
        val interpreter = try {
            getModelFromFiles()?.let { Interpreter(it) } ?: return PhishingAttemptLevel.NONE
        } catch (e: Exception) {
            return PhishingAttemptLevel.NONE
        }

        val input = FloatBuffer.allocate(interpreter.getInputTensor(0).numElements())
        urlToArray(url, getModelCharacterMap(), input)
        val output: FloatBuffer = FloatBuffer.allocate(interpreter.getOutputTensor(0).numElements())
        try {
            interpreter.run(input, output)

            
            val phishingProbability = output[1]
            return levelFromProbability(phishingProbability)
        } catch (exception: java.lang.Exception) {
        }
        return PhishingAttemptLevel.NONE
    }

    private fun levelFromProbability(
        probability: Float
    ): PhishingAttemptLevel = when {
        probability > 0.9 -> PhishingAttemptLevel.HIGH
        probability > 0.7 -> PhishingAttemptLevel.MODERATE
        else -> PhishingAttemptLevel.NONE
    }

    private fun getModelCharacterMap(): Map<String, Int> {
        val jsonText = context.assets.open("tokenizer.json").bufferedReader().use { it.readText() }
        val gson = Gson()
        val mapType = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(jsonText, mapType)
    }

    private suspend fun getModelFromFiles(): File? {
        return antiPhishingFilesDownloader.getPhishingModelFile()
            .runIfNull {
            }
    }

    private fun urlToArray(url: String, characterMap: Map<String, Int>, input: FloatBuffer) {
        val urlFormatted = url.substring(0, min(url.length, MAX_LENGTH))

        
        var id = MAX_LENGTH - 1
        var numCharacters = urlFormatted.length - 1
        while (numCharacters >= 0) {
            val character = urlFormatted[numCharacters]
            input.put(id, characterMap[character.toString()]?.toFloat() ?: 0F)

            numCharacters -= 1
            id -= 1
        }

        
        for (i in 0..id) {
            input.put(i, 0F)
        }
    }

    companion object {
        private const val MAX_LENGTH = 200
    }
}