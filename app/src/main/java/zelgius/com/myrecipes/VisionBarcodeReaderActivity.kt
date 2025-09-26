package zelgius.com.myrecipes

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraUnavailableException
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.viewfinder.compose.CoordinateTransformer
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Cameraswitch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import zelgius.com.myrecipes.ui.AppTheme
import zelgius.com.myrecipes.ui.common.autosizetext.toIntOffset
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
class VisionBarcodeReaderActivity : AppCompatActivity() {
    private val viewModel by viewModels<CameraViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val lifecycleOwner = LocalLifecycleOwner.current

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    viewModel.startCamera(lifecycleOwner)
                } else {
                    Toast.makeText(
                        this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            val result by viewModel.scanResult.collectAsStateWithLifecycle()
            LaunchedEffect(result) {
                if (result != null) {
                    setResult(RESULT_OK, Intent().putExtra("BASE64", result))
                    finish()
                }
            }

            // Request camera permissions
            LaunchedEffect(Unit) {
                if (ActivityCompat.checkSelfPermission(
                        this@VisionBarcodeReaderActivity,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    viewModel.startCamera(lifecycleOwner)
                } else {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            }

            AppTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
                    surfaceRequest?.let { request ->
                        val coordinateTransformer = remember { MutableCoordinateTransformer() }
                        CameraXViewfinder(
                            surfaceRequest = request,
                            coordinateTransformer = coordinateTransformer,
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(viewModel, coordinateTransformer) {
                                    detectTapGestures(
                                        onTap = {
                                            viewModel.focusAt(coordinateTransformer, it)
                                        }
                                    )

                                }
                        )
                    }

                    val coordinates by viewModel.coordinates.collectAsStateWithLifecycle()
                    coordinates?.let {
                        Box(
                            Modifier
                                .size(48.dp)
                                .offset { it.toIntOffset() }
                                .offset((-24).dp, (-24).dp)
                                .background(
                                    shape = CircleShape, brush = Brush.radialGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                    SmallFloatingActionButton(
                        modifier = Modifier
                            .align(TopEnd)
                            .padding(top = 32.dp, end = 16.dp),
                        onClick = {
                            viewModel.changeCamera(lifecycleOwner)
                        }
                    ) {
                        Icon(Icons.TwoTone.Cameraswitch, contentDescription = null)
                    }
                }
            }
        }

    }
}


@HiltViewModel
class CameraViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var surfaceMeteringPointFactory: SurfaceOrientedMeteringPointFactory? = null

    // Used to set up a link between the Camera and your UI.
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult: StateFlow<String?> = _scanResult

    private var camera: Camera? = null

    private val cameraPreviewUseCase = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
            surfaceMeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                newSurfaceRequest.resolution.width.toFloat(),
                newSurfaceRequest.resolution.height.toFloat()
            )
        }
    }

    val detector by lazy {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_AZTEC
                )
                .build()
        )
    }

    val listener: (List<Barcode>) -> Unit = {
        for (barcode in it) {
            when (barcode.valueType) {
                Barcode.TYPE_TEXT -> {
                    _scanResult.value = barcode.rawValue
                }
            }
        }
    }

    private val _coordinates = MutableStateFlow<Offset?>(null)
    val coordinates: StateFlow<Offset?> = _coordinates

    fun changeCamera(lifecycleOwner: LifecycleOwner) {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else
            CameraSelector.DEFAULT_BACK_CAMERA

        startCamera(lifecycleOwner)
    }

    //region Camera setup
    fun startCamera(lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Select back camera as a default

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor(), QRImageAnalyzer())
                }

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    cameraPreviewUseCase, /*imageCapture ,*/
                    imageAnalyzer,
                )

            } catch (exc: Exception) {
                exc.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun focusAt(coordinateTransformer: CoordinateTransformer, offset: Offset) {
        _coordinates.value = offset

        val coordinates = with(coordinateTransformer) { offset.transform() }
        val autoFocusPoint: MeteringPoint =
            surfaceMeteringPointFactory?.createPoint(coordinates.x, coordinates.y) ?: return

        try {
            camera?.cameraControl?.startFocusAndMetering(
                FocusMeteringAction.Builder(
                    autoFocusPoint,
                    FocusMeteringAction.FLAG_AF
                ).build()
            )

        } catch (e: CameraUnavailableException) {
            e.printStackTrace()
        }

        viewModelScope.launch {
            delay(2000)
            _coordinates.value = null
        }
    }

    private inner class QRImageAnalyzer : ImageAnalysis.Analyzer {

        @androidx.annotation.OptIn(ExperimentalGetImage::class)
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image

            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                detector.process(image)
                    .addOnSuccessListener {
                        listener(it)
                        mediaImage.close()
                        imageProxy.close()
                    }
                    .addOnFailureListener {
                        mediaImage.close()
                        it.printStackTrace()
                    }
                    .addOnCanceledListener {
                        mediaImage.close()
                        imageProxy.close()
                    }
                    .addOnCompleteListener {
                        mediaImage.close()
                        imageProxy.close()
                    }

            } else
                imageProxy.close()
        }
    }
}