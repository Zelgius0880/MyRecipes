package zelgius.com.myrecipes.ui.play.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.model.Recipe
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.data.repository.RecipeRepository
import zelgius.com.myrecipes.data.text
import zelgius.com.myrecipes.data.useCase.GetInstructionsUseCase
import zelgius.com.myrecipes.data.useCase.IngredientInstruction
import zelgius.com.myrecipes.data.useCase.InstructionItem
import zelgius.com.myrecipes.data.useCase.StepInstruction
import zelgius.com.myrecipes.repository.TextToSpeechRepository
import javax.inject.Inject

@HiltViewModel
class PlayRecipeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getInstructionUseCase: GetInstructionsUseCase,
    private val recipeRepository: RecipeRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val textToSpeechRepository: TextToSpeechRepository
) : ViewModel() {
    private val _instructions = MutableStateFlow(emptyList<InstructionItem>())
    val instructions = _instructions.asStateFlow()

    val readingEnabled get() = dataStoreRepository.isTextReadingChecked

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe = _recipe.asStateFlow()

    private var selectedItem: InstructionItem? = null

    fun load(id: Long) {
        viewModelScope.launch {
            _recipe.value = recipeRepository.getFull(id)
            _instructions.value = getInstructionUseCase.execute(id)
            instructions.value.firstOrNull()?.let {
                onInstructionSelected(it)
            }
        }
    }

    fun onReadingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.setTextReadingChecked(enabled)
        }
    }

    fun onInstructionSelected(instruction: InstructionItem) {
        selectedItem = instruction

        viewModelScope.launch {
            val isReadingEnabled = readingEnabled.first()
            if (!isReadingEnabled) return@launch

            when (instruction) {
                is IngredientInstruction -> {
                    textToSpeechRepository.speak(instruction.ingredient.text(context, false))
                }

                is StepInstruction -> {
                    textToSpeechRepository.speak(instruction.step.text)
                }
            }
        }
    }
}