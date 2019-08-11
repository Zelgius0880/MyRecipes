/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package zelgius.com.barcodereader

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import kotlinx.android.synthetic.main.activity_codebar_reader.*

import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Activity for the multi-tracker app.  This app detects barcodes and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and ID of each barcode.
 */
class BarcodeReaderActivity : AppCompatActivity() {
    private var context: Context? = null

    // CAMERA VERSION ONE DECLARATIONS
    private var mCameraSource: CameraSource? = null

    // CAMERA VERSION TWO DECLARATIONS
    private var mCamera2Source: Camera2Source? = null

    // COMMON TO BOTH CAMERAS
    private var mPreview: CameraSourcePreview? = null
    private var previewFaceDetector: FaceDetector? = null
    private var mGraphicOverlay: GraphicOverlay<GraphicOverlay.Graphic>? = null
    private var mFaceGraphic: FaceGraphic? = null
    private var wasActivityResumed = false
    private var isRecordingVideo = false
    private var takePictureButton: Button? = null
    private var switchButton: Button? = null
    private var videoButton: Button? = null

    // DEFAULT CAMERA BEING OPENED
    private var usingFrontCamera = true

    // MUST BE CAREFUL USING THIS VARIABLE.
    // ANY ATTEMPT TO START CAMERA2 ON API < 21 WILL CRASH.
    private var useCamera2 = false

    private val cameraSourceShutterCallback: CameraSource.ShutterCallback =
        object : CameraSource.ShutterCallback {
            override fun onShutter() {
                Log.d(TAG, "Shutter Callback!")
            }
        }
    private val cameraSourcePictureCallback: CameraSource.PictureCallback =
        object : CameraSource.PictureCallback {
            override fun onPictureTaken(pic: Bitmap) {
                Log.d(TAG, "Taken picture is here!")
                runOnUiThread {
                    switchButton!!.isEnabled = true
                    videoButton!!.isEnabled = true
                    takePictureButton!!.isEnabled = true
                }
                var out: FileOutputStream? = null
                try {
                    out = FileOutputStream(
                        File(
                            Environment.getExternalStorageDirectory(),
                            "/camera_picture.png"
                        )
                    )
                    pic.compress(Bitmap.CompressFormat.JPEG, 95, out)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        out?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    private val cameraSourceVideoStartCallback: CameraSource.VideoStartCallback =
        object : CameraSource.VideoStartCallback {
            override fun onVideoStart() {
                isRecordingVideo = true
                runOnUiThread {
                    videoButton!!.isEnabled = true
                    videoButton!!.text = getString(R.string.stop_video)
                }
                Toast.makeText(context, "Video STARTED!", Toast.LENGTH_SHORT).show()
            }
        }
    private val cameraSourceVideoStopCallback: CameraSource.VideoStopCallback =
        object : CameraSource.VideoStopCallback {
            override fun onVideoStop(videoFile: String?) {
                isRecordingVideo = false
                runOnUiThread {
                    switchButton!!.isEnabled = true
                    takePictureButton!!.isEnabled = true
                    videoButton!!.isEnabled = true
                    videoButton!!.text = getString(R.string.record_video)
                }
                Toast.makeText(context, "Video STOPPED!", Toast.LENGTH_SHORT).show()
            }
        }
    private val cameraSourceVideoErrorCallback: CameraSource.VideoErrorCallback =
        object : CameraSource.VideoErrorCallback {
            override fun onVideoError(error: String?) {
                isRecordingVideo = false
                runOnUiThread {
                    switchButton!!.isEnabled = true
                    takePictureButton!!.isEnabled = true
                    videoButton!!.isEnabled = true
                    videoButton!!.text = getString(R.string.record_video)
                }
                Toast.makeText(context, "Video Error: " + error!!, Toast.LENGTH_LONG).show()
            }
        }
    private val camera2SourceVideoStartCallback: Camera2Source.VideoStartCallback =
        object : Camera2Source.VideoStartCallback {
            override fun onVideoStart() {
                isRecordingVideo = true
                runOnUiThread {
                    videoButton!!.isEnabled = true
                    videoButton!!.text = getString(R.string.stop_video)
                }
                Toast.makeText(context, "Video STARTED!", Toast.LENGTH_SHORT).show()
            }
        }
    private val camera2SourceVideoStopCallback: Camera2Source.VideoStopCallback =
        object : Camera2Source.VideoStopCallback {
            override fun onVideoStop(videoFile: String?) {
                isRecordingVideo = false
                runOnUiThread {
                    switchButton!!.isEnabled = true
                    takePictureButton!!.isEnabled = true
                    videoButton!!.isEnabled = true
                    videoButton!!.text = getString(R.string.record_video)
                }
                Toast.makeText(context, "Video STOPPED!", Toast.LENGTH_SHORT).show()
            }
        }
    private val camera2SourceVideoErrorCallback: Camera2Source.VideoErrorCallback =
        object : Camera2Source.VideoErrorCallback {
            override fun onVideoError(error: String) {
                isRecordingVideo = false
                runOnUiThread {
                    switchButton!!.isEnabled = true
                    takePictureButton!!.isEnabled = true
                    videoButton!!.isEnabled = true
                    videoButton!!.text = getString(R.string.record_video)
                }
                Toast.makeText(context, "Video Error: $error", Toast.LENGTH_LONG).show()
            }
        }

    private val camera2SourceShutterCallback: Camera2Source.ShutterCallback =
        object : Camera2Source.ShutterCallback {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onShutter() {
                Log.d(TAG, "Shutter Callback for CAMERA2")
            }
        }

    private val camera2SourcePictureCallback: Camera2Source.PictureCallback =
        object : Camera2Source.PictureCallback {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPictureTaken(image: Image) {
                Log.d(TAG, "Taken picture is here!")
                runOnUiThread {
                    switchButton!!.isEnabled = true
                    videoButton!!.isEnabled = true
                    takePictureButton!!.isEnabled = true
                }
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.capacity())
                buffer.get(bytes)
                val picture = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                var out: FileOutputStream? = null
                try {
                    out = FileOutputStream(
                        File(
                            Environment.getExternalStorageDirectory(),
                            "/camera2_picture.png"
                        )
                    )
                    picture.compress(Bitmap.CompressFormat.JPEG, 95, out)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        out?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }

    private val cameraPreviewTouchListener = View.OnTouchListener { v, pEvent ->
        v.onTouchEvent(pEvent)
        if (pEvent.action == MotionEvent.ACTION_DOWN) {
            val autoFocusX = (pEvent.x - Utils.dpToPx(60) / 2).toInt()
            val autoFocusY = (pEvent.y - Utils.dpToPx(60) / 2).toInt()
            ivAutoFocus!!.translationX = autoFocusX.toFloat()
            ivAutoFocus!!.translationY = autoFocusY.toFloat()
            ivAutoFocus!!.visibility = View.VISIBLE
            ivAutoFocus!!.bringToFront()
            if (useCamera2) {
                if (mCamera2Source != null) {
                    mCamera2Source!!.autoFocus(object : Camera2Source.AutoFocusCallback {
                        override fun onAutoFocus(success: Boolean) {
                            runOnUiThread { ivAutoFocus!!.visibility = View.GONE }
                        }
                    }, pEvent, v.width, v.height)
                } else {
                    ivAutoFocus!!.visibility = View.GONE
                }
            } else {
                if (mCameraSource != null) {
                    mCameraSource!!.autoFocus(object : CameraSource.AutoFocusCallback {
                        override fun onAutoFocus(success: Boolean) {
                            runOnUiThread { ivAutoFocus!!.visibility = View.GONE }
                        }
                    })
                } else {
                    ivAutoFocus!!.visibility = View.GONE
                }
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_codebar_reader)
        context = applicationContext

        takePictureButton = btn_takepicture
        switchButton = btn_switch
        videoButton = btn_video
        mPreview = preview
        mGraphicOverlay = findViewById(R.id.faceOverlay)

        if (checkGooglePlayAvailability()) {
            requestPermissionThenOpenCamera()

            switchButton!!.setOnClickListener {
                usingFrontCamera = if (usingFrontCamera) {
                    stopCameraSource()
                    createCameraSourceBack()
                    false
                } else {
                    stopCameraSource()
                    createCameraSourceFront()
                    true
                }
            }

            takePictureButton!!.setOnClickListener {
                switchButton!!.isEnabled = false
                videoButton!!.isEnabled = false
                takePictureButton!!.isEnabled = false
                if (useCamera2) {
                    if (mCamera2Source != null) mCamera2Source!!.takePicture(
                        camera2SourceShutterCallback,
                        camera2SourcePictureCallback
                    )
                } else {
                    if (mCameraSource != null) mCameraSource!!.takePicture(
                        cameraSourceShutterCallback,
                        cameraSourcePictureCallback
                    )
                }
            }

            videoButton!!.setOnClickListener {
                switchButton!!.isEnabled = false
                takePictureButton!!.isEnabled = false
                videoButton!!.isEnabled = false
                if (isRecordingVideo) {
                    if (useCamera2) {
                        if (mCamera2Source != null) mCamera2Source!!.stopVideo()
                    } else {
                        if (mCameraSource != null) mCameraSource!!.stopVideo()
                    }
                } else {
                    if (useCamera2) {
                        if (mCamera2Source != null) mCamera2Source!!.recordVideo(
                            camera2SourceVideoStartCallback,
                            camera2SourceVideoStopCallback,
                            camera2SourceVideoErrorCallback
                        )
                    } else {
                        if (mCameraSource != null) mCameraSource!!.recordVideo(
                            cameraSourceVideoStartCallback,
                            cameraSourceVideoStopCallback,
                            cameraSourceVideoErrorCallback
                        )
                    }
                }
            }

            mPreview!!.run { setOnTouchListener(cameraPreviewTouchListener) }
        }
    }

    private fun checkGooglePlayAvailability(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        if (resultCode == ConnectionResult.SUCCESS) {
            return true
        } else {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode, 2404).show()
            }
        }
        return false
    }

    private fun requestPermissionThenOpenCamera() {
        if (ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(
                    context!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                useCamera2 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                createCameraSourceFront()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_PERMISSION
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    private fun createCameraSourceFront() {
        previewFaceDetector = FaceDetector.Builder(context)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .setMode(FaceDetector.FAST_MODE)
            .setProminentFaceOnly(true)
            .setTrackingEnabled(true)
            .build()

        if (previewFaceDetector!!.isOperational) {
            previewFaceDetector!!.setProcessor(MultiProcessor.Builder(GraphicFaceTrackerFactory()).build())
        } else {
            Toast.makeText(context, "FACE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show()
        }

        if (useCamera2) {
            mCamera2Source = Camera2Source.Builder(context, previewFaceDetector)
                .setFocusMode(Camera2Source.CAMERA_AF_AUTO)
                .setFlashMode(Camera2Source.CAMERA_FLASH_AUTO)
                .setFacing(Camera2Source.CAMERA_FACING_FRONT)
                .build()

            //IF CAMERA2 HARDWARE LEVEL IS LEGACY, CAMERA2 IS NOT NATIVE.
            //WE WILL USE CAMERA1.
            if (mCamera2Source!!.isCamera2Native) {
                startCameraSource()
            } else {
                useCamera2 = false
                if (usingFrontCamera) createCameraSourceFront() else createCameraSourceBack()
            }
        } else {
            mCameraSource = CameraSource.Builder(context, previewFaceDetector)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build()

            startCameraSource()
        }
    }

    private fun createCameraSourceBack() {
        previewFaceDetector = FaceDetector.Builder(context)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .setMode(FaceDetector.FAST_MODE)
            .setProminentFaceOnly(true)
            .setTrackingEnabled(true)
            .build()

        if (previewFaceDetector!!.isOperational) {
            previewFaceDetector!!.setProcessor(MultiProcessor.Builder(GraphicFaceTrackerFactory()).build())
        } else {
            Toast.makeText(context, "FACE DETECTION NOT AVAILABLE", Toast.LENGTH_SHORT).show()
        }

        if (useCamera2) {
            mCamera2Source = Camera2Source.Builder(context, previewFaceDetector)
                .setFocusMode(Camera2Source.CAMERA_AF_AUTO)
                .setFlashMode(Camera2Source.CAMERA_FLASH_AUTO)
                .setFacing(Camera2Source.CAMERA_FACING_BACK)
                .build()

            //IF CAMERA2 HARDWARE LEVEL IS LEGACY, CAMERA2 IS NOT NATIVE.
            //WE WILL USE CAMERA1.
            if (mCamera2Source!!.isCamera2Native) {
                startCameraSource()
            } else {
                useCamera2 = false
                if (usingFrontCamera) createCameraSourceFront() else createCameraSourceBack()
            }
        } else {
            mCameraSource = CameraSource.Builder(context, previewFaceDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .build()

            startCameraSource()
        }
    }

    private fun startCameraSource() {
        if (useCamera2) {
            if (mCamera2Source != null) {
                cameraVersion!!.text = "Camera 2"
                try {
                    mPreview!!.start(mCamera2Source!!, mGraphicOverlay!!)
                } catch (e: IOException) {
                    Log.e(TAG, "Unable to start camera source 2.", e)
                    mCamera2Source!!.release()
                    mCamera2Source = null
                }

            }
        } else {
            if (mCameraSource != null) {
                cameraVersion!!.text = "Camera 1"
                try {
                    mPreview!!.start(mCameraSource!!, mGraphicOverlay!!)
                } catch (e: IOException) {
                    Log.e(TAG, "Unable to start camera source.", e)
                    mCameraSource!!.release()
                    mCameraSource = null
                }

            }
        }
    }

    private fun stopCameraSource() {
        mPreview!!.stop()
    }

    private inner class GraphicFaceTrackerFactory : MultiProcessor.Factory<Face> {
        override fun create(face: Face): Tracker<Face> {
            return GraphicFaceTracker(mGraphicOverlay!!)
        }
    }

    private inner class GraphicFaceTracker internal constructor(private val mOverlay: GraphicOverlay<GraphicOverlay.Graphic>) :
        Tracker<Face>() {

        init {
            mFaceGraphic = FaceGraphic(mOverlay, context!!)
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        override fun onNewItem(faceId: Int, item: Face?) {
            mFaceGraphic!!.setId(faceId)
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        override fun onUpdate(detectionResults: Detector.Detections<Face>?, face: Face?) {
            mOverlay.add(mFaceGraphic!!)
            mFaceGraphic!!.updateFace(face!!)
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        override fun onMissing(detectionResults: Detector.Detections<Face>?) {
            mFaceGraphic!!.goneFace()
            mOverlay.remove(mFaceGraphic!!)
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        override fun onDone() {
            mFaceGraphic!!.goneFace()
            mOverlay.remove(mFaceGraphic!!)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionThenOpenCamera()
            } else {
                Toast.makeText(this, "CAMERA PERMISSION REQUIRED", Toast.LENGTH_LONG).show()
                finish()
            }
        }
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermissionThenOpenCamera()
            } else {
                Toast.makeText(this, "STORAGE PERMISSION REQUIRED", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (wasActivityResumed)
        //If the CAMERA2 is paused then resumed, it won't start again unless creating the whole camera again.
            if (useCamera2) {
                if (usingFrontCamera) {
                    createCameraSourceFront()
                } else {
                    createCameraSourceBack()
                }
            } else {
                startCameraSource()
            }
    }

    override fun onPause() {
        super.onPause()
        wasActivityResumed = true
        stopCameraSource()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCameraSource()
        if (previewFaceDetector != null) {
            previewFaceDetector!!.release()
        }
    }

    companion object {
        private const val TAG = "Ezequiel Adrian Camera"
        private const val REQUEST_CAMERA_PERMISSION = 200
        private const val REQUEST_STORAGE_PERMISSION = 201
    }
}