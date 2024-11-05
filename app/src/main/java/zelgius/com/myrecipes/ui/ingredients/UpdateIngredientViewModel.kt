package zelgius.com.myrecipes.ui.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.DefaultIngredients
import zelgius.com.myrecipes.data.model.SimpleIngredient
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.data.repository.IngredientRepository
import zelgius.com.myrecipes.worker.WorkerRepository
import javax.inject.Inject

@HiltViewModel
class UpdateIngredientViewModel @Inject constructor(
    private val ingredientRepository: IngredientRepository,
    dataStoreRepository: DataStoreRepository,
    private var workerRepository: WorkerRepository
) : ViewModel() {
    private val _ingredientFlow = MutableStateFlow<SimpleIngredient?>(null)
    val ingredientFlow = _ingredientFlow.filterNotNull()

    val isIaGenerationEnabled = dataStoreRepository.isIAGenerationChecked.map { true }
    lateinit var initialIngredient: SimpleIngredient

    fun init(ingredient: SimpleIngredient) {
        _ingredientFlow.value = ingredient
        initialIngredient = ingredient
    }

    fun onNameChanged(name: String) {
        _ingredientFlow.value = _ingredientFlow.value?.copy(name = name)
    }

    fun onPromptChanged(prompt: String) {
        _ingredientFlow.value = _ingredientFlow.value?.copy(prompt = prompt)
    }

    fun onGenerationEnabledChanged(enabled: Boolean) {
        _ingredientFlow.value = _ingredientFlow.value?.copy(generationEnabled = enabled)
    }

    fun onImageChanged(image: DefaultIngredients?) {
        _ingredientFlow.value = _ingredientFlow.value?.copy(imageUrl = image?.url)
    }

    fun save() {
        viewModelScope.launch {
            _ingredientFlow.value?.let {
                ingredientRepository.update(it)

                if (it.generationEnabled && it.imageUrl == null)
                    workerRepository.startIaGenerationWorker(true)
            }
        }
    }

    fun onGeneratedImageReset() {
        _ingredientFlow.value = _ingredientFlow.value?.copy(imageUrl = null)
        viewModelScope.launch {
            ingredientRepository.update(initialIngredient.copy(imageUrl = null))
            if (initialIngredient.generationEnabled)
                workerRepository.startIaGenerationWorker(true)

        }
    }
}
