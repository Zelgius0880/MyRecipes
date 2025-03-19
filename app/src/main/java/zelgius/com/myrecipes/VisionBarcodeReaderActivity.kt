package zelgius.com.myrecipes

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import zelgius.com.myrecipes.ui.AppTheme
import java.util.concurrent.Executors
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
class VisionBarcodeReaderActivity : AppCompatActivity() {

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var camera: Camera

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

            // See API reference for complete list of supported types
            when (barcode.valueType) {
                Barcode.TYPE_TEXT -> {
                    barcode.rawValue
                    setResult(RESULT_OK, Intent().putExtra("BASE64", barcode.rawValue))
                    finish()
                }
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            var coordinates by remember {
                mutableStateOf<Pair<Float, Float>?>(null)
            }

            val previewView = remember {
                PreviewView(this).apply {
                    setOnTouchListener { v, event ->
                        when (event.action) {
                            MotionEvent.ACTION_UP -> true
                            MotionEvent.ACTION_DOWN -> {
                                val factory = SurfaceOrientedMeteringPointFactory(
                                    v.width.toFloat(),
                                    v.height.toFloat()
                                )
                                val autoFocusPoint: MeteringPoint =
                                    factory.createPoint(event.x, event.y)
                                try {
                                    camera.cameraControl.startFocusAndMetering(
                                        FocusMeteringAction.Builder(
                                            autoFocusPoint,
                                            FocusMeteringAction.FLAG_AF
                                        ).disableAutoCancel().build()
                                    )

                                    coordinates = event.x to event.y
                                } catch (e: CameraUnavailableException) {
                                    e.printStackTrace()
                                }

                                true
                            }

                            else -> false
                        }
                    }
                }
            }

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    startCamera(previewView)
                } else {
                    Toast.makeText(
                        this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT
                    ).show()
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
                    previewView.post { startCamera(previewView) }
                } else {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            }


            LaunchedEffect(coordinates) {
                delay(1000)
                coordinates = null
            }
            AppTheme {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )

                    coordinates?.let { (x, y) ->
                        val sizePx = with(LocalDensity.current) {
                            24.dp.toPx()
                        }
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset((x - sizePx / 2).roundToInt(),( y - sizePx / 2).roundToInt())
                                }
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                .blur(radius = 16.dp)
                        )

                    }

                    SmallFloatingActionButton(
                        modifier = Modifier
                            .align(TopEnd)
                            .padding(top = 32.dp, end = 16.dp),
                        onClick = {
                            cameraSelector =
                                if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                else
                                    CameraSelector.DEFAULT_BACK_CAMERA

                            startCamera(previewView)
                        }
                    ) {
                        Icon(Icons.TwoTone.Cameraswitch, contentDescription = null)
                    }
                }
            }
        }
    }


    //region Camera setup
    private fun startCamera(previewView: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

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
                    this, cameraSelector, preview, /*imageCapture ,*/imageAnalyzer,
                )

            } catch (exc: Exception) {
                exc.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
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