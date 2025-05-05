package com.luke.pager.screens.quotescreen.imageprocessing

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.google.mlkit.vision.text.Text

@Composable
fun ScanCanvas(
    modifier: Modifier,
    textBlocks: List<Text.TextBlock>,
    clusteredBlocks: List<Text.TextBlock>,
    imageWidth: Int,
    imageHeight: Int
) {
    Canvas(modifier = modifier) {
        for (block in clusteredBlocks) {
            for (line in block.lines) {
                val points = line.cornerPoints ?: continue
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
                    color = Color.Green,
                    style = Stroke(width = 3f)
                )
            }
        }

        val clusteredCandidates = textBlocks.filter { it.boundingBox != null }
        val ignoredBlocks = clusteredCandidates.filterNot { clusteredBlocks.contains(it) }

        for (block in ignoredBlocks) {
            for (line in block.lines) {
                val points = line.cornerPoints ?: continue
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
                    color = Color.Red,
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}
