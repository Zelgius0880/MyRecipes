package zelgius.com.myrecipes.ui.addFromWeb.viewModel

import androidx.lifecycle.ViewModel
import com.zelgius.myrecipes.ia.usecase.DataExtractionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import zelgius.com.myrecipes.data.model.Recipe
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AddFromWebViewModel @Inject constructor(
    private val dataExtractionUseCase: DataExtractionUseCase
) : ViewModel() {
    private val _url = MutableStateFlow<String>("")
    val url = _url.asStateFlow()

    private val _loading = MutableStateFlow<Boolean>(false)
    val loading = _loading.asStateFlow()

    fun onUrlChanged(url: String) {
        _url.value = url
    }

    suspend fun startExtraction(bytes: ByteArray, locale: Locale): Recipe? {
        _loading.value = true

        return try {
            dataExtractionUseCase.execute(bytes, locale.language).getOrThrow()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }.also{
            _loading.value = false
        }
    }

    fun onDispose() {
        _loading.value = false
        _url.value = ""
    }
}