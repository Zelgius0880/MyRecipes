package zelgius.com.myrecipes.ui.play.viewModel

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
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
import zelgius.com.myrecipes.mediapipe.usecase.LiveGestureRecognitionUseCase
import zelgius.com.myrecipes.repository.Gesture
import zelgius.com.myrecipes.repository.TextToSpeechRepository
import javax.inject.Inject
import kotlin.collections.lastIndex

@HiltViewModel
class PlayRecipeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getInstructionUseCase: GetInstructionsUseCase,
    private val recipeRepository: RecipeRepository,
    private val dataStoreRepository: DataStoreRepository,
    private val textToSpeechRepository: TextToSpeechRepository,
    private val gestureRecognitionUseCase: LiveGestureRecognitionUseCase
) : ViewModel() {
    private val _instructions = MutableStateFlow(emptyList<InstructionItem>())
    val instructions = _instructions.asStateFlow()

    val readingEnabled get() = dataStoreRepository.isTextReadingChecked
    val gestureRecognitionEnabled get() = dataStoreRepository.isGestureRecognitionChecked

    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe = _recipe.asStateFlow()

    private var selectedItem: InstructionItem? = null

    private val _currentInstructionPosition = MutableStateFlow<Int>(0)
    val currentInstructionPosition = _currentInstructionPosition.asStateFlow()

    init {
        viewModelScope.launch {
            gestureRecognitionUseCase.gestureFlow.collect {
                when (it) {
                    Gesture.ThumbUp -> onNext()
                    Gesture.ThumbDown -> onPreview()
                    Gesture.PointingUp, Gesture.ClosedFist -> onInstructionSelected(currentInstructionPosition.value)
                }
            }
        }

    }

    fun load(id: Long) {
        viewModelScope.launch {
            _recipe.value = recipeRepository.getFull(id)
            _instructions.value = getInstructionUseCase.execute(id)
            instructions.value.firstOrNull()?.let {
                onInstructionSelected(it)
            }
        }
    }

    fun startGestureRecognition(lifecycleOwner: LifecycleOwner, preview: PreviewView?, orientation: Int) {
        viewModelScope.launch {
            gestureRecognitionUseCase.execute(orientation, preview , lifecycleOwner)
        }
    }

    fun onReadingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.setTextReadingChecked(enabled)
        }
    }

    fun onGestureRecognitionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.setGestureRecognitionChecked(enabled)
        }
    }

    fun onNext() {
        var currentItemPosition = currentInstructionPosition.value
        if (currentItemPosition < instructions.value.lastIndex) {
            ++currentItemPosition
            _currentInstructionPosition.value = currentItemPosition
            onInstructionSelected(instructions.value[currentItemPosition])
        }
    }

    fun onPreview() {
        var currentItemPosition = currentInstructionPosition.value
        if (currentItemPosition >= 1) {
            --currentItemPosition
            _currentInstructionPosition.value = currentItemPosition
            onInstructionSelected(instructions.value[currentItemPosition])
        }
    }

    fun onInstructionSelected(index: Int) {
        _currentInstructionPosition.value = index
        onInstructionSelected(instructions.value[index])
    }

    private fun onInstructionSelected(instruction: InstructionItem) {
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

    override fun onCleared() {
        super.onCleared()
        gestureRecognitionUseCase.clear()
    }

    fun cancelRecognition() {
        gestureRecognitionUseCase.clear()
    }
}