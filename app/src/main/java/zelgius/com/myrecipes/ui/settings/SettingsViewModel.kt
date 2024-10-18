package zelgius.com.myrecipes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import zelgius.com.myrecipes.worker.ImageGenerationWorker
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {
    private val _isIAGenerationEnabled = MutableStateFlow(false)
    val isIAGenerationEnabled = _isIAGenerationEnabled.asStateFlow()

    val isIAGenerationChecked get() = dataStoreRepository.isIAGenerationChecked

    init {
        _isIAGenerationEnabled.value = ImageGenerationWorker.modelExists
    }

    fun setIsIAGenerationChecked(checked: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.setIAGenerationChecked(checked)
        }
    }

}