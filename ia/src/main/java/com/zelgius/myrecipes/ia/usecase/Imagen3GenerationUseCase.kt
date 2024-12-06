package com.zelgius.myrecipes.ia.usecase

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONObject
import kotlin.io.encoding.ExperimentalEncodingApi

class Imagen3GenerationUseCase () {

    enum class TargetType {
        Ingredient, Recipe
    }

    @Serializable
    data class Response(
        val images: List<ByteArray>
    )

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun execute(name: String): List<Bitmap> {

        val response = Firebase.functions
            .getHttpsCallable("generateIngredientImage")
            .call(
                mapOf(
                    "name" to name
                ),
            )
            .continueWith { task ->
                @Suppress("UNCHECKED_CAST")
                val result = (task.result?.getData() as Map<String, Any>)
                // convert data to json string
                val jsonString = JSONObject(result).toString()

                Json.decodeFromString<Response>(jsonString)
            }.await()

        val results = mutableListOf<Bitmap>()
        response.images.forEach {
            val options = BitmapFactory.Options().apply {
                inMutable = true
            }

            results.add(BitmapFactory.decodeByteArray(it, 0, it.size, options))
        }

        return results
    }
}