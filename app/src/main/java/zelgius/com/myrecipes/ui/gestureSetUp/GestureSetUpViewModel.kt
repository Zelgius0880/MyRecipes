package zelgius.com.myrecipes.ui.gestureSetUp

import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zelgius.myrecipes.ia.repository.Landmark
import com.zelgius.myrecipes.ia.usecase.LiveGestureRecognitionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.data.repository.DataStoreRepository
import javax.inject.Inject

@HiltViewModel
class GestureSetUpViewModel @Inject constructor(
    private val gestureRecognitionUseCase: LiveGestureRecognitionUseCase,
    private val dataStoreRepository: DataStoreRepository
) : ViewModel() {
    val gestureDetected =  gestureRecognitionUseCase.gestureFlow.map { it != null }

    private val _lastLandmarksDetected = MutableStateFlow<List<List<Landmark>>>(emptyList())
    val lastLandmarksDetected = _lastLandmarksDetected.asStateFlow()

    val gestureDetectionArea = dataStoreRepository.gestureDetectionAreaPercent

    init {
        viewModelScope.launch {
            gestureRecognitionUseCase.landMarksFlow.collect {
                _lastLandmarksDetected.value = it
            }
        }
    }

    fun submitGestureDetectionArea(value : Float) {
        viewModelScope.launch {
            dataStoreRepository.setGestureDetectionMaxZ(value)
        }
    }

    fun startDetection(
        owner: LifecycleOwner, previewView: PreviewView, orientation: Int, width: Double, height: Double
    ) {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
            exception.printStackTrace()
            gestureRecognitionUseCase.clear()
        }
        viewModelScope.launch(coroutineExceptionHandler) {
            gestureRecognitionUseCase.execute(
                orientation,
                width,
                height,
                previewView,
                owner,
                cooldown = 0
            )
        }
    }
}