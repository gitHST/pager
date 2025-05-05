package com.luke.pager.screens.quotescreen.imageprocessing

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.google.mlkit.vision.text.Text

@Composable
fun ScanCanvas(
    modifier: Modifier,
    allClusters: List<List<Text.TextBlock>>,
    imageWidth: Int,
    imageHeight: Int
) {
    val clusterColors = listOf(
        Color.Green,
        Color.Red,
        Color.Blue,
        Color.Magenta,
        Color.Cyan,
        Color.Yellow,
        Color.Gray,
        Color.LightGray,
        Color.DarkGray
    )

    // 🔥 Log cluster count once when allClusters changes
    LaunchedEffect(allClusters) {
        Log.d("ScanCanvas", "Total clusters detected: ${allClusters.size}")
    }

    Canvas(modifier = modifier) {
        allClusters.forEachIndexed { index, cluster ->
            val color = clusterColors.getOrElse(index) { Color.Black }

            for (block in cluster) {
                // for (line in block.lines) {
                    val points = block.cornerPoints ?: continue
                    if (points.size < 4) continue

                    val scaledPoints = points.map { point ->
                        Offset(
                            x = point.x.toFloat() * size.width / imageWidth,
                            y = point.y.toFloat() * size.height / imageHeight
                        )
                    }

                    val path = Path().apply {
                        moveTo(scaledPoints[0].x, scaledPoints[0].y)
                        for (i in 1 until scaledPoints.size) {
                            lineTo(scaledPoints[i].x, scaledPoints[i].y)
                        }
                        close()
                    }

                    drawPath(
                        path = path,
                        color = color,
                        style = Stroke(width = if (index == 0) 3f else 2f)
                    )
                // }
            }
        }
    }
}
