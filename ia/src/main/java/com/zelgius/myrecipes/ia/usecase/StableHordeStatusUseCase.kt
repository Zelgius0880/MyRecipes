package com.zelgius.myrecipes.ia.usecase

import com.zelgius.myrecipes.ia.repository.StableHordeRepository
import zelgius.com.myrecipes.data.model.ImageGenerationRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StableHordeStatusUseCase @Inject constructor(
    private val stableHordeRepository: StableHordeRepository,
) {
    suspend fun execute(
        request: ImageGenerationRequest,
    ): ImageGenerationRequest {

        val processingResponse = stableHordeRepository.getRequestCheck(
            id = request.requestId,
        )

        return request.copy(
            remainingTime = processingResponse.waitTime.toLong(),
            status = when {
                processingResponse.queuePosition > 0 -> ImageGenerationRequest.Status.Waiting
                processingResponse.processing == 1 -> ImageGenerationRequest.Status.Processing
                else -> ImageGenerationRequest.Status.Done
            }
        )
    }
}

enum class StableHordeStyle(val id: String) {
    Realistic1("b22740f2-48a1-4860-a1ab-d31981017e23"),
    Realistic2("0a59d9c0-090a-4e8e-a663-6efd9c2d52dd"),
    Drawing1("bebb4dd8-d05b-4ddb-998e-e0fc98d93c1e"),
    Drawing2("04e0f6a0-0807-4b3a-819e-aee299ad5c31"),
    Drawing3("4e909c3f-db66-45f7-87c9-8cffa2a53d22"),
}