package com.luke.pager.screens.quotescreen.scan.imageprocessing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2

data class ClusterResult(
    val textBlocks: List<Text.TextBlock>,
    val imageWidth: Int,
    val imageHeight: Int,
    val rotatedBitmap: Bitmap,
    val allClusters: List<List<Text.TextBlock>>,
    val clusterDistances: List<ClusterDistanceDebug>
)

data class ClusterDistanceDebug(
    val clusterA: Int,
    val clusterB: Int,
    val distance: Float
)

suspend fun processImageAndCluster(
    context: Context,
    uri: Uri,
    eps: Float = 20f,
    minPts: Int = 1
): ClusterResult {
    // 1️⃣ Load bitmap (no EXIF rotation)
    val bitmapStream = context.contentResolver.openInputStream(uri)
    val bitmap = BitmapFactory.decodeStream(bitmapStream)
    bitmapStream?.close()

    // 2️⃣ First OCR pass: detect text orientation
    val tempImage = InputImage.fromBitmap(bitmap, 0)
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
        } else {
            null
        }
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

    Log.d("ImageTextProcessor", "OCR found ${finalTextBlocks.size} text blocks")

    // 7️⃣ Build DBSCANBlockBox list
    val allBoxes = mutableListOf<DBSCANBlockBox>()
    for (block in finalTextBlocks) {
        val boundingBox = block.boundingBox ?: continue
        val lineRects = block.lines.mapNotNull { it.boundingBox }
        allBoxes.add(DBSCANBlockBox(block, boundingBox, lineRects))
    }

    // 8️⃣ Run DBSCAN
    val (clusters, _) = dbscan2D(allBoxes, eps = eps, minPts = minPts)
    val mergedClusters = mergeOverlappingClusters<DBSCANBlockBox>(clusters.map { it.toList() }).map { it.toList() }

    val textClusters = mergedClusters.map { it.map { box -> box.block } }

    val debugDistances = mutableListOf<ClusterDistanceDebug>()

    if (mergedClusters.size > 1) {
        var overallMinDistance = Float.MAX_VALUE
        for (i in mergedClusters.indices) {
            for (j in i + 1 until mergedClusters.size) {
                val dist = minDistanceBetweenClusters(mergedClusters[i], mergedClusters[j])
                Log.d("ImageTextProcessor", "Distance between Cluster $i and Cluster $j: $dist")

                debugDistances.add(ClusterDistanceDebug(i, j, dist))

                if (dist < overallMinDistance) {
                    overallMinDistance = dist
                }
            }
        }
        Log.d("ImageTextProcessor", "Overall shortest distance between any two clusters: $overallMinDistance")
    } else {
        Log.d("ImageTextProcessor", "Only one cluster found; no inter-cluster distance to log.")
    }

    return ClusterResult(
        textBlocks = finalTextBlocks,
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        rotatedBitmap = correctedBitmap,
        allClusters = textClusters,
        clusterDistances = debugDistances
    )
}

fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun <T> mergeOverlappingClusters(clusters: List<Collection<T>>): List<Set<T>> {
    val merged = mutableListOf<MutableSet<T>>()

    for (cluster in clusters) {
        val overlapping = mutableListOf<MutableSet<T>>()

        for (existing in merged) {
            if (existing.any { it in cluster }) {
                overlapping.add(existing)
            }
        }

        if (overlapping.isEmpty()) {
            merged.add(cluster.toMutableSet())
        } else {
            val newCluster = overlapping.flatMap { it }.toMutableSet()
            newCluster.addAll(cluster)
            merged.removeAll(overlapping)
            merged.add(newCluster)
        }
    }

    return merged
}
