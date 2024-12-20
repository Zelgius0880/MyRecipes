package zelgius.com.myrecipes.data.repository.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import zelgius.com.myrecipes.data.entities.ImageGenerationRequestEntity

@Dao
interface ImageGenerationProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: ImageGenerationRequestEntity)

    @Update
    suspend fun update(progress: ImageGenerationRequestEntity)

    @Delete
    suspend fun delete(progress: ImageGenerationRequestEntity)

    @Query("SELECT * FROM ImageGenerationRequest LIMIT 1")
    suspend fun get(): ImageGenerationRequestEntity?

    @Query("DELETE FROM ImageGenerationRequest")
    suspend fun delete(): Int

}