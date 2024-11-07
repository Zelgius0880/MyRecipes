package zelgius.com.myrecipes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.model.SimpleIngredient
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.worker.ImageGenerationWorker
import zelgius.com.myrecipes.worker.WorkerRepository
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val ingredientRepository: IngredientRepository,
    private val workRepository: WorkerRepository
) : ViewModel() {
    private val _isIAGenerationEnabled = MutableStateFlow(false)
    val isIAGenerationEnabled = _isIAGenerationEnabled.asStateFlow()
    val ingredients = ingredientRepository.getSimpleIngredients()

    val isIAGenerationChecked get() = dataStoreRepository.isIAGenerationChecked

    init {
        _isIAGenerationEnabled.value = ImageGenerationWorker.modelExists
    }

    fun setIsIAGenerationChecked(checked: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.setIAGenerationChecked(checked)
            if(checked) workRepository.startIaGenerationWorker(true)
        }
    }

    fun deleteIngredient(ingredient: SimpleIngredient) {
        viewModelScope.launch {
            ingredientRepository.deleteIngredient(ingredient.id)
        }
    }

}