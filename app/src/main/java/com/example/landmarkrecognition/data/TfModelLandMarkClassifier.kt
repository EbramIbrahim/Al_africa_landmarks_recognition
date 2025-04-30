package com.example.landmarkrecognition.data

import android.content.Context
import android.graphics.Bitmap
import android.view.Surface
import com.example.landmarkrecognition.domain.Classification
import com.example.landmarkrecognition.domain.LandMarkClassifier
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class TfModelLandMarkClassifier(
    private val context: Context,
    private val threshold: Float = 0.5f,
    private val maxResult: Int = 1
) : LandMarkClassifier {

    private var imageClassifier: ImageClassifier? = null

    private fun setupClassifier() {
        val baseOptions = BaseOptions.builder()
            .setNumThreads(2)
            .build()

        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setMaxResults(maxResult)
            .setScoreThreshold(threshold)
            .build()

        try {

            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                "landmark.tflite",
                options
            )

        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun classify(
        bitmap: Bitmap,
        rotation: Int
    ): List<Classification> {
        if (imageClassifier == null) {
            setupClassifier()
        }
        val imageProcess = ImageProcessor.Builder().build()
        val tensorImage =  imageProcess.process(TensorImage.fromBitmap(bitmap))

        val imageProcessingOption = ImageProcessingOptions.builder()
            .setOrientation(getOrientationFromRotation(rotation))
            .build()

        val results = imageClassifier?.classify(tensorImage, imageProcessingOption)

        return results?.flatMap { classification ->
            classification.categories.map { category ->
                Classification(
                    name = category.displayName,
                    score = category.score
                )
            }
        }?.distinctBy { it.name } ?: emptyList()

    }

    private fun getOrientationFromRotation(rotation: Int): ImageProcessingOptions.Orientation {
        return when(rotation) {
            Surface.ROTATION_270 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            else -> ImageProcessingOptions.Orientation.RIGHT_TOP
        }
    }

}







