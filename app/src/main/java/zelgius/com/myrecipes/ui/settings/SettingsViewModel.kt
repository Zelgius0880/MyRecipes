package zelgius.com.myrecipes.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {
    private val _isIAGenerationEnabled = MutableStateFlow(false)
    val isIAGenerationEnabled = _isIAGenerationEnabled.asStateFlow()

    val isIAGenerationChecked get() = dataStoreRepository.isIAGenerationChecked

    init {
        _isIAGenerationEnabled.value = File("/data/local/tmp/image_generator/").exists()
    }

    fun setIsIAGenerationChecked(checked: Boolean) {
        viewModelScope.launch {
            dataStoreRepository.setIAGenerationChecked(checked)
        }
    }

}