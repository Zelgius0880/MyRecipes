package com.zelgius.myrecipes.ia.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.zelgius.myrecipes.ia.model.stableHorde.request.GenerationInputStable
import com.zelgius.myrecipes.ia.model.stableHorde.response.RequestStatusStable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.example.model.response.RequestAsync
import org.example.model.response.RequestStatusCheck
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import zelgius.com.myrecipes.data.logger.Logger
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StableHordeRepository @Inject constructor(
    private val httpClient: OkHttpClient
) {

    private val json = Json { ignoreUnknownKeys = true }
    private val contentType = "application/json".toMediaType()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()

    private val service = retrofit.create<StableHordeApiService>()


    suspend fun getRequestStatus(
        id: String,
    ): RequestStatusStable {
        return tryCalling { service.getRequestStatus(id) }
    }

    suspend fun getRequestCheck(
        id: String,
    ): RequestStatusCheck {
        return tryCalling { service.getRequestCheck(id) }
    }

    suspend fun postGenerateAsync(
        generationInput: GenerationInputStable,
        xFields: String? = null,
        apiKey: String
    ): RequestAsync {
        return tryCalling { service.postGenerateAsync(generationInput, xFields, apiKey) }
    }

    suspend fun downloadImage(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(imageUrl)
            .build()

        val outputStream = ByteArrayOutputStream()
        try {
            val response: Response = tryCalling { httpClient.newCall(request).execute() }

            if (response.isSuccessful) {
                response.body?.byteStream()?.use { inputStream ->
                    outputStream.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                val bytes = outputStream.toByteArray()
                return@withContext BitmapFactory.decodeByteArray(
                    bytes,
                    0,
                    bytes.size,
                ) // Download successful
            } else {
                // Handle unsuccessful responses (e.g., log error)
                Logger.e("Download failed: ${response.code} - ${response.message}")  // Log or handle the error
                return@withContext null
            }

        } catch (e: Exception) {
            // Handle exceptions during download (e.g., network issues)
            Logger.e(throwable = e)  // Log the error
            return@withContext null
        }
    }

    private suspend fun <T> tryCalling(retries: Int = 0, work: suspend () -> T): T {
        return try {
            work()
        } catch (e: Exception) {
            if (retries > MAX_RETRY) throw e
            else {
                delay(5000)
                tryCalling(retries + 1, work)
            }
        }
    }

    companion object {
        const val BASE_URL = "https://stablehorde.net/api/"
        const val MAX_RETRY = 5
    }

    interface StableHordeApiService {

        @GET("v2/generate/status/{id}")
        suspend fun getRequestStatus(
            @Path("id") id: String,
        ): RequestStatusStable

        @GET("v2/generate/check/{id}")
        suspend fun getRequestCheck(
            @Path("id") id: String,
        ): RequestStatusCheck

        @POST("v2/generate/async")
        suspend fun postGenerateAsync(
            @Body generationInput: GenerationInputStable,
            @Header("X-Fields") xFields: String? = null,
            @Header("apikey") apiKey: String
        ): RequestAsync
    }

}