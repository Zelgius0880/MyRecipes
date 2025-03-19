package zelgius.com.myrecipes.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zelgius.billing.repository.BillingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.model.PlayRecipeStepPosition
import zelgius.com.myrecipes.data.model.SimpleIngredient
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.data.repository.DatabaseRepository
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
    billingRepository: BillingRepository,
    private val generatePdfUseCase: GeneratePdfUseCase,
    private val workRepository: WorkerRepository,
    private val databaseRepository: DatabaseRepository,
) : ViewModel() {

    private val ingredients = ingredientRepository.getSimpleIngredients()

    private val isIAGenerationChecked =
        dataStoreRepository.isIAGenerationChecked.combine(billingRepository.isPremium) { isChecked, isPremium ->
            isChecked && isPremium
        }

    private val exportingProgress = MutableStateFlow<Float?>(null)

    private val _settingsUiState = MutableStateFlow(SettingsUiState())
        .combine(ingredients) { state, ingredients ->
            state.copy(ingredients = ingredients)
        }
        .combine(isIAGenerationChecked) { state, isChecked ->
            state.copy(isIAGenerationChecked = isChecked)
        }
        .combine(exportingProgress) { state, progress ->
            state.copy(exportingProgress = progress)
        }
        .combine(dataStoreRepository.playRecipeStepPosition) { state, position ->
            state.copy(playRecipeStepPosition = position)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    val settingsUiState = _settingsUiState

    fun setIsIAGenerationChecked(checked: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.setIAGenerationChecked(checked)
            if (checked) workRepository.startOrScheduleIaGenerationWorker(resetStatus = true)
        }
    }

    fun setPlayRecipeStepPosition(position: PlayRecipeStepPosition) {
        viewModelScope.launch {
            dataStoreRepository.setPlayRecipeStepPosition(position)
        }
    }

    fun deleteIngredient(ingredient: SimpleIngredient) {
        viewModelScope.launch {
            ingredientRepository.deleteIngredient(ingredient.id)
        }
    }

    suspend fun importDatabase(uri: Uri): Boolean {
        return uri.path?.let {
            databaseRepository.restoreDatabase(backup = context.contentResolver.openInputStream(uri)?: return@let false)
        } == true
    }

    suspend fun exportDatabase(uri: Uri): Boolean = uri.path?.let {
        databaseRepository.backupDatabase(context.contentResolver.openOutputStream(uri)?: return@let false)
    } == true

    suspend fun exportAllRecipes(outputStream: OutputStream) {
        exportingProgress.value = 0f
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
                exportingProgress.value = (index + 1f) / recipes.size
            }

        zipOut.close()
        exportingProgress.value = null
    }

    fun generateImageNow() {
        viewModelScope.launch {
            workRepository.startIaGenerationImmediately()
        }
    }
}

data class SettingsUiState(
    val isIAGenerationChecked: Boolean = false,
    val exportingProgress: Float? = 0f,
    val ingredients: List<SimpleIngredient> = emptyList(),
    val playRecipeStepPosition: PlayRecipeStepPosition = PlayRecipeStepPosition.Last,
)