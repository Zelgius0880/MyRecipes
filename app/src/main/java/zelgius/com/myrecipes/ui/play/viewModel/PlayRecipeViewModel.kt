package zelgius.com.myrecipes.ui.play.viewModel

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zelgius.billing.repository.BillingRepository
import com.zelgius.myrecipes.ia.repository.Gesture
import com.zelgius.myrecipes.ia.usecase.LiveGestureRecognitionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val billingRepository: BillingRepository,
    private val textToSpeechRepository: TextToSpeechRepository,
    private val gestureRecognitionUseCase: LiveGestureRecognitionUseCase
) : ViewModel() {
    private val _instructions = MutableStateFlow(emptyList<InstructionItem>())
    val instructions = _instructions.asStateFlow()

    val readingEnabled get() = dataStoreRepository.isTextReadingChecked
    val gestureRecognitionEnabled
        get() = dataStoreRepository.isGestureRecognitionChecked
            .combine(billingRepository.isPremium) { isChecked, isPremium ->
            isChecked && isPremium
        }

    private val _gestureRecognitionError = MutableStateFlow(false)
    val gestureRecognitionError get() = _gestureRecognitionError.asStateFlow()

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
                    Gesture.PointingUp, Gesture.ClosedFist -> onInstructionSelected(
                        currentInstructionPosition.value
                    )
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

    fun startGestureRecognition(
        lifecycleOwner: LifecycleOwner,
        preview: PreviewView?,
        orientation: Int
    ) {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
            gestureRecognitionUseCase.clear()
            _gestureRecognitionError.value = true
        }
        viewModelScope.launch(coroutineExceptionHandler) {
            gestureRecognitionUseCase.execute(
                orientation,
                preview,
                lifecycleOwner
            )
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
        cancelRecognition()
    }

    fun cancelRecognition() {
        gestureRecognitionUseCase.clear()
    }
}