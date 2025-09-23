package zelgius.com.myrecipes.worker

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import zelgius.com.myrecipes.data.repository.RecipeRepository
import java.io.File
import javax.inject.Inject

class CheckMissingImageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recipeRepository: RecipeRepository,
) {

    suspend fun execute() {
        recipeRepository.get().forEach { recipe ->
            recipe.imageURL?.let {
                if (!imageExists(it)) recipeRepository.update(recipe.copy(imageURL = null))
            }
        }
    }

    private fun imageExists(url: String) = try {
        context.contentResolver.openOutputStream(url.toUri())?.use { } != null || File(url).exists()
    } catch (_: Exception) {
        false
    }
}