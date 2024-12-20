package com.zelgius.myrecipes.ia.usecase

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class FrenchToEnglishUseCase {
    val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.FRENCH)
        .setTargetLanguage(TranslateLanguage.ENGLISH)
        .build()

    private val frenchEnglishTranslator = Translation.getClient(options)

    suspend fun execute(text: String): String {
        if(!Locale.getDefault().language.startsWith(Locale.FRENCH.language)) return text

        if(!downloadIfNeeded()) return text

        return translate(text).also{
            frenchEnglishTranslator.close()
        }?: text
    }


    private suspend fun downloadIfNeeded() = suspendCancellableCoroutine<Boolean>{ continuation ->
        frenchEnglishTranslator.downloadModelIfNeeded()
            .addOnSuccessListener {
                continuation.resume(true)
            }
            .addOnFailureListener {
                continuation.resume(false)
            }
    }

    private suspend fun translate(text: String) = suspendCancellableCoroutine<String?> { continuation ->
        frenchEnglishTranslator.translate(text)
            .addOnSuccessListener {
                continuation.resume(it)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }
}