package zelgius.com.myrecipes.ui.play.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.useCase.GetInstructionsUseCase
import zelgius.com.myrecipes.data.useCase.InstructionItem
import javax.inject.Inject

@HiltViewModel
class PlayRecipeViewModel @Inject constructor(
    private val getRecipeUseCase: GetInstructionsUseCase
) : ViewModel() {
    private val _instructions = MutableStateFlow(emptyList<InstructionItem>())
    val instructions = _instructions.asStateFlow()

    fun load(id: Long) {
        viewModelScope.launch {
            _instructions.value = getRecipeUseCase.execute(id)
        }
    }
}