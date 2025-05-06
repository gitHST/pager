package com.luke.pager.screens.quotescreen.scan

import android.graphics.Point
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.google.mlkit.vision.text.Text
import com.luke.pager.screens.quotescreen.scan.imageprocessing.computeConvexHull
import com.luke.pager.screens.quotescreen.scan.staticdataclasses.OutlineLevel

@Composable
fun ScanOutlineCanvas(
    modifier: Modifier,
    allClusters: List<List<Text.TextBlock>>,
    imageWidth: Int,
    imageHeight: Int,
    outlineLevel: OutlineLevel
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

    LaunchedEffect(allClusters) {
        Log.d("ScanCanvas", "Total clusters detected: ${allClusters.size}")
    }

    Canvas(modifier = modifier) {
        fun drawShape(points: Array<Point>, color: Color, clusterIndex: Int) {
            if (points.size < 4) return

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
                color = color.copy(alpha = 0.2f),
                style = Stroke(width = 0f)
            )
            drawPath(
                path = path,
                color = color.copy(alpha = 0.2f),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 5f)
            )

            val labelPosition = scaledPoints.minByOrNull { it.y } ?: scaledPoints[0]
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    clusterIndex.toString(),
                    labelPosition.x,
                    labelPosition.y - 5,
                    android.graphics.Paint().apply {
                        this.color = android.graphics.Color.BLACK
                        textSize = 30f
                        isAntiAlias = true
                        setShadowLayer(4f, 0f, 0f, android.graphics.Color.WHITE)
                    }
                )
            }
        }



        allClusters.forEachIndexed { index, cluster ->
            val color = clusterColors.getOrElse(index) { Color.Black }

            if (outlineLevel == OutlineLevel.CLUSTER) {
                val linePoints = mutableListOf<Point>()
                for (block in cluster) {
                    for (line in block.lines) {
                        line.cornerPoints?.let { linePoints.addAll(it) }
                    }
                }
                if (linePoints.size >= 3) {
                    val hull = computeConvexHull(linePoints)
                    drawShape(hull.toTypedArray(), color, index)
                }
            } else {
                for (block in cluster) {
                    when (outlineLevel) {
                        OutlineLevel.BLOCK -> {
                            val linePoints = mutableListOf<Point>()
                            for (line in block.lines) {
                                line.cornerPoints?.let { linePoints.addAll(it) }
                            }
                            if (linePoints.size >= 3) {
                                val hull = computeConvexHull(linePoints)
                                drawShape(hull.toTypedArray(), color, index)
                            }
                        }

                        OutlineLevel.LINE -> {
                            for (line in block.lines) {
                                val wordPoints = mutableListOf<Point>()
                                for (element in line.elements) {
                                    element.cornerPoints?.let { wordPoints.addAll(it) }
                                }
                                if (wordPoints.size >= 3) {
                                    val hull = computeConvexHull(wordPoints)
                                    drawShape(hull.toTypedArray(), color, index)
                                }
                            }
                        }

                        OutlineLevel.WORD -> {
                            for (line in block.lines) {
                                for (element in line.elements) {
                                    val points = element.cornerPoints ?: continue
                                    drawShape(points, color, index)
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}
