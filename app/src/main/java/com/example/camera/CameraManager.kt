package com.example.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.model.CameraLensFacing
import com.example.model.RecordingQuality
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor

/**
 * Controller for CameraX preview and video recording pipeline.
 * Encapsulates binding, lens switching, and file recording safely.
 */
class CameraManager(private val context: Context) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    fun initialize(onInitialized: (Boolean) -> Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            try {
                cameraProvider = future.get()
                onInitialized(true)
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization failed", e)
                onInitialized(false)
            }
        }, mainExecutor)
    }

    fun startPreview(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: CameraLensFacing,
        quality: RecordingQuality,
        analyzer: androidx.camera.core.ImageAnalysis.Analyzer? = null,
        onBoundSuccess: () -> Unit,
        onBoundFailure: (String) -> Unit
    ) {
        val provider = cameraProvider
        if (provider == null) {
            onBoundFailure("Camera provider not initialized yet.")
            return
        }

        try {
            provider.unbindAll()

            val cameraSelector = if (lensFacing == CameraLensFacing.FRONT) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            // Check if lens is actually available on device
            if (!provider.hasCamera(cameraSelector)) {
                onBoundFailure("Selected lens ($lensFacing) is not available on this device.")
                return
            }

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val targetQuality = if (quality == RecordingQuality.HIGH) {
                QualitySelector.from(Quality.FHD, androidx.camera.video.FallbackStrategy.higherQualityOrLowerThan(Quality.HD))
            } else {
                QualitySelector.from(Quality.HD, androidx.camera.video.FallbackStrategy.lowerQualityOrHigherThan(Quality.SD))
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(targetQuality)
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            if (analyzer != null) {
                val imageAnalysis = androidx.camera.core.ImageAnalysis.Builder()
                    .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()
                imageAnalysis.setAnalyzer(mainExecutor, analyzer)

                try {
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        videoCapture,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    // Fall back to binding without analyzer if hardware doesn't support 3 simultaneous use-cases
                    Log.w(TAG, "Binding with ImageAnalysis failed, falling back to Preview + VideoCapture", e)
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        videoCapture
                    )
                }
            } else {
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
            }
            onBoundSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind camera use cases", e)
            onBoundFailure(e.localizedMessage ?: "Failed to start camera preview.")
        }
    }

    fun startRecording(
        recordAudio: Boolean,
        onEvent: (VideoRecordEvent) -> Unit
    ): File? {
        val capture = videoCapture ?: return null
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outputDir = context.getExternalFilesDir(null) ?: context.filesDir
        val videoFile = File(outputDir, "tex_record_${timeStamp}.mp4")

        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        var pendingRecording = capture.output
            .prepareRecording(context, outputOptions)

        if (recordAudio) {
            try {
                pendingRecording = pendingRecording.withAudioEnabled()
            } catch (e: SecurityException) {
                Log.w(TAG, "Audio permission not granted for recording", e)
            }
        }

        activeRecording = pendingRecording.start(mainExecutor) { event ->
            onEvent(event)
        }
        return videoFile
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    fun stopCamera() {
        try {
            activeRecording?.stop()
            activeRecording = null
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping camera", e)
        }
    }

    companion object {
        private const val TAG = "CameraManager"
    }
}
