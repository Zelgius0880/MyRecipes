package zelgius.com.myrecipes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Cameraswitch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors


@OptIn(ExperimentalMaterial3Api::class)
class VisionBarcodeReaderActivity : AppCompatActivity() {

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val previewView = remember {
                PreviewView(this)
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
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                previewView.post { startCamera(previewView) }
            } else {
                launcher.launch(Manifest.permission.CAMERA)
            }


            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize()
                )

                SmallFloatingActionButton(
                    modifier = Modifier
                        .align(TopEnd)
                        .padding(top = 16.dp, end = 16.dp),
                    onClick = {
                        if(cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                        else
                            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        startCamera(previewView)
                    }
                ) {
                    Icon(Icons.TwoTone.Cameraswitch, contentDescription = null)
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
                    it.setSurfaceProvider(previewView.surfaceProvider)
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
                cameraProvider.bindToLifecycle(
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