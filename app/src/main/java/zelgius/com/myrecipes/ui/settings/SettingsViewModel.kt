package zelgius.com.myrecipes.ui.settings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.model.SimpleIngredient
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.useCase.pdf.GeneratePdfUseCase
import zelgius.com.myrecipes.worker.WorkerRepository
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStoreRepository: DataStoreRepository,
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val generatePdfUseCase: GeneratePdfUseCase,
    private val workRepository: WorkerRepository
) : ViewModel() {
    private val _isIAGenerationEnabled = MutableStateFlow(false)
    val isIAGenerationEnabled = _isIAGenerationEnabled.asStateFlow()
    val ingredients = ingredientRepository.getSimpleIngredients()

    val isIAGenerationChecked
        get() = dataStoreRepository.isIAGenerationChecked.map {
            it && (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                    || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
        }

    private val _exportingProgress = MutableStateFlow<Float?>(null)
    val exportingProgress = _exportingProgress.asStateFlow()

    init {
        _isIAGenerationEnabled.value = true // TODO
    }

    fun setIsIAGenerationChecked(checked: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.setIAGenerationChecked(checked)
            if (checked) workRepository.startOrScheduleIaGenerationWorker(resetStatus = true)
        }
    }

    fun deleteIngredient(ingredient: SimpleIngredient) {
        viewModelScope.launch {
            ingredientRepository.deleteIngredient(ingredient.id)
        }
    }

    suspend fun exportAllRecipes(outputStream: OutputStream) {
        _exportingProgress.value = 0f
        val recipes = recipeRepository.get()
        val zipOut = ZipOutputStream(outputStream)

        val entries = mutableMapOf<String, Int>()
        recipes.mapNotNull { recipeRepository.getFull(it.id ?: 0) }
            .forEachIndexed { index, recipe ->
                val entry = ZipEntry(
                    "${
                        recipe.name.replace(
                            File.separator,
                            "_"
                        )
                    }${entries[recipe.name]?.let { " ($it)" } ?: ""}.pdf"
                )
                zipOut.putNextEntry(entry)
                entries[recipe.name] = (entries[recipe.name] ?: 0) + 1

                generatePdfUseCase.execute(recipe, zipOut, false)
                _exportingProgress.value = (index + 1f) / recipes.size
            }

        zipOut.close()
        _exportingProgress.value = null
    }

    fun generateImageNow() {
        workRepository.startIaGenerationImmediately()
        //context.startForegroundService(Intent(context, ImageGenerationService::class.java))
    }

}