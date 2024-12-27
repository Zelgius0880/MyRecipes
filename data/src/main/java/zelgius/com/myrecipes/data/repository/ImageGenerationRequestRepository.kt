package zelgius.com.myrecipes.data.repository

import zelgius.com.myrecipes.data.entities.toModel
import zelgius.com.myrecipes.data.model.ImageGenerationRequest
import zelgius.com.myrecipes.data.model.toEntity
import zelgius.com.myrecipes.data.repository.dao.ImageGenerationProgressDao
import javax.inject.Inject

class ImageGenerationRequestRepository @Inject constructor(
    private val imageGenerationProgressDao: ImageGenerationProgressDao
) {
    suspend fun get() = imageGenerationProgressDao.get()?.toModel()

    suspend fun update(progress: ImageGenerationRequest) =
        imageGenerationProgressDao.update(progress.toEntity())

    suspend fun insert(progress: ImageGenerationRequest) =
        imageGenerationProgressDao.insert(progress.toEntity())

    suspend fun delete(progress: ImageGenerationRequest) =
        imageGenerationProgressDao.delete(progress.toEntity())

    suspend fun delete() =
        imageGenerationProgressDao.delete()
}