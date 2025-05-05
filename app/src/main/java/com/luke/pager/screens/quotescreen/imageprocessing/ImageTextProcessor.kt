package com.luke.pager.screens.quotescreen.imageprocessing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2

data class ClusterResult(
    val textBlocks: List<Text.TextBlock>,
    val clusteredBlocks: List<Text.TextBlock>,
    val imageWidth: Int,
    val imageHeight: Int,
    val rotatedBitmap: Bitmap,
    val allClusters: List<List<Text.TextBlock>>
)


suspend fun processImageAndCluster(
    context: Context,
    uri: Uri,
    eps: Float = 100f,
    minPts: Int = 3
): ClusterResult {
    // 1️⃣ Load bitmap (no EXIF rotation)
    val bitmapStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(bitmapStream)
    bitmapStream?.close()

    // 2️⃣ First OCR pass: detect text orientation
    val tempImage = InputImage.fromBitmap(bitmap, 0)  // No rotation
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val result = recognizer.process(tempImage).await()
    val textBlocks = result.textBlocks

    // 3️⃣ Detect dominant angle
    val angles = textBlocks.mapNotNull { block ->
        val corners = block.cornerPoints
        if (corners != null && corners.size >= 2) {
            val p0 = corners[0]
            val p1 = corners[1]
            val dx = (p1.x - p0.x).toFloat()
            val dy = (p1.y - p0.y).toFloat()
            Math.toDegrees(atan2(dy, dx).toDouble())
        } else null
    }

    val avgAngle = if (angles.isNotEmpty()) angles.average() else 0.0

    // 4️⃣ Decide rotation based on angle
    val rotationDegrees = when {
        avgAngle in -45.0..45.0 -> 0
        avgAngle in 45.0..135.0 -> -90
        avgAngle in -135.0..-45.0 -> 90
        else -> 180
    }

    // 5️⃣ Rotate bitmap if needed
    val correctedBitmap = if (rotationDegrees != 0) {
        rotateBitmap(bitmap, rotationDegrees.toFloat())
    } else {
        bitmap
    }

    // 6️⃣ OCR pass #2: on the rotated image
    val finalImage = InputImage.fromBitmap(correctedBitmap, 0)
    val finalResult = recognizer.process(finalImage).await()
    val finalTextBlocks = finalResult.textBlocks
    val imageWidth = finalImage.width
    val imageHeight = finalImage.height

    // 7️⃣ Build BlockBox list
    val allBoxes = mutableListOf<BlockBox>()
    for (block in finalTextBlocks) {
        val boundingBox = block.boundingBox ?: continue
        allBoxes.add(BlockBox(block, boundingBox))
    }

    // 8️⃣ Run DBSCAN
    val (clusters, _) = dbscan2D(allBoxes, eps = eps, minPts = minPts)

    // 9️⃣ Pick preferred cluster (center bias)
    val clusteredBlocks = if (clusters.isNotEmpty()) {
        val imageCenterX = imageWidth / 2f
        val centerThreshold = imageWidth * 0.25f  // 25% overlap buffer

        fun getClusterBoundingBox(cluster: List<BlockBox>): Rect {
            val first = cluster.first().rect
            var left = first.left
            var top = first.top
            var right = first.right
            var bottom = first.bottom

            for (box in cluster) {
                val rect = box.rect
                if (rect.left < left) left = rect.left
                if (rect.top < top) top = rect.top
                if (rect.right > right) right = rect.right
                if (rect.bottom > bottom) bottom = rect.bottom
            }

            return Rect(left, top, right, bottom)
        }

        val overlappingCenter = clusters.filter { cluster ->
            val bbox = getClusterBoundingBox(cluster)
            bbox.left < imageCenterX + centerThreshold && bbox.right > imageCenterX - centerThreshold
        }

        val preferredCluster = overlappingCenter.maxByOrNull { it.size }
            ?: clusters.maxByOrNull { it.size }

        preferredCluster?.map { it.block } ?: emptyList()
    } else {
        emptyList()
    }

    val textClusters = clusters.map { it.map { box -> box.block } }

    return ClusterResult(
        textBlocks = finalTextBlocks,
        clusteredBlocks = clusteredBlocks,
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        rotatedBitmap = correctedBitmap,
        allClusters = textClusters
    )
}

// 🔄 Bitmap rotation helper
fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}
