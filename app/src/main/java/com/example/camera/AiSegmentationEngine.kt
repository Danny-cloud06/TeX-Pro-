package com.example.camera

import android.graphics.Bitmap
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMask
import com.google.mlkit.vision.segmentation.Segmenter
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions

/**
 * On-device AI Person Segmentation Engine using Google ML Kit.
 * Runs completely locally on Android hardware with zero server uploads,
 * separating the foreground presenter from the live camera stream.
 */
class AiSegmentationEngine {
    private var segmenter: Segmenter? = null
    private var isClosed = false

    init {
        try {
            val options = SelfieSegmenterOptions.Builder()
                .setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)
                .enableRawSizeMask()
                .build()
            segmenter = Segmentation.getClient(options)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ML Kit selfie segmenter", e)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    fun processImageProxy(
        imageProxy: ImageProxy,
        onSuccess: (SegmentationMask) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val mediaImage = imageProxy.image
        val client = segmenter
        if (mediaImage == null || isClosed || client == null) {
            imageProxy.close()
            return
        }

        try {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            client.process(inputImage)
                .addOnSuccessListener { mask ->
                    onSuccess(mask)
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing camera frame", e)
            imageProxy.close()
        }
    }

    fun processBitmap(
        bitmap: Bitmap,
        rotationDegrees: Int = 0,
        onSuccess: (SegmentationMask) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val client = segmenter ?: return
        if (isClosed) return

        try {
            val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)
            client.process(inputImage)
                .addOnSuccessListener { mask ->
                    onSuccess(mask)
                }
                .addOnFailureListener { e ->
                    onError(e)
                }
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun close() {
        isClosed = true
        try {
            segmenter?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing segmenter", e)
        }
        segmenter = null
    }

    companion object {
        private const val TAG = "AiSegmentationEngine"
    }
}
