package com.luke.pager.screens.quotescreen.scan

import android.graphics.Point
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.google.mlkit.vision.text.Text
import com.luke.pager.screens.quotescreen.scan.imageprocessing.computeConvexHull
import com.luke.pager.screens.quotescreen.scan.imageprocessing.staticdataclasses.OutlineLevel

@Composable
fun ScanOutlineCanvas(
    modifier: Modifier,
    pageIndex: Int,
    allClusters: List<List<Text.TextBlock>>,
    imageWidth: Int,
    imageHeight: Int,
    outlineLevel: OutlineLevel,
    toggledClusters: Set<Int>,
    globalClusterOrder: List<Pair<Int, Int>>,
    onClusterClick: ((Int) -> Unit)? = null,
) {
    val clusterColors =
        listOf(
            Color(0xFFFF2828),
            Color(0xFFF46C36),
            Color(0xFFF39F21),
            Color(0xFFFBD240),
            Color(0xFFDCFF3C),
            Color(0xFF93FF48),
            Color(0xFF50FF76),
            Color(0xFF4AFFD3),
            Color(0xFF4FC1FF),
            Color(0xFF4F95FF),
            Color(0xFF4F7BFF),
            Color(0xFF614FFF),
            Color(0xFFA14FFF),
            Color(0xFFD04FFF),
            Color(0xFFFF4FD3),
            Color(0xFFFF4F95),
        )

    val showLabels = globalClusterOrder.size > 1

    Canvas(
        modifier =
            modifier.pointerInput(allClusters, toggledClusters) {
                detectTapGestures { offset ->
                    val x = offset.x.toInt()
                    val y = offset.y.toInt()
                    allClusters.forEachIndexed { index, cluster ->
                        val linePoints = mutableListOf<Point>()
                        for (block in cluster) {
                            for (line in block.lines) {
                                line.cornerPoints?.let { linePoints.addAll(it) }
                            }
                        }
                        if (linePoints.size >= 3) {
                            val hull = computeConvexHull(linePoints)
                            val scaledPoints =
                                hull.map { point ->
                                    Offset(
                                        x = point.x.toFloat() * size.width / imageWidth,
                                        y = point.y.toFloat() * size.height / imageHeight,
                                    )
                                }
                            val path =
                                Path().apply {
                                    moveTo(scaledPoints[0].x, scaledPoints[0].y)
                                    for (i in 1 until scaledPoints.size) {
                                        lineTo(scaledPoints[i].x, scaledPoints[i].y)
                                    }
                                    close()
                                }

                            val androidPath = path.asAndroidPath()
                            val bounds = android.graphics.RectF()
                            @Suppress("DEPRECATION")
                            androidPath.computeBounds(bounds, true)
                            val region =
                                android.graphics.Region().apply {
                                    setPath(
                                        androidPath,
                                        android.graphics.Region(
                                            bounds.left.toInt(),
                                            bounds.top.toInt(),
                                            bounds.right.toInt(),
                                            bounds.bottom.toInt(),
                                        ),
                                    )
                                }
                            if (region.contains(x, y)) {
                                onClusterClick?.invoke(index)
                            }
                        }
                    }
                }
            },
    ) {
        fun drawShape(
            points: Array<Point>,
            color: Color,
            clusterIndex: Int,
        ) {
            if (points.size < 4) return

            val scaledPoints =
                points.map { point ->
                    Offset(
                        x = point.x.toFloat() * size.width / imageWidth,
                        y = point.y.toFloat() * size.height / imageHeight,
                    )
                }

            val path =
                Path().apply {
                    moveTo(scaledPoints[0].x, scaledPoints[0].y)
                    for (i in 1 until scaledPoints.size) {
                        lineTo(scaledPoints[i].x, scaledPoints[i].y)
                    }
                    close()
                }

            val isToggled = toggledClusters.contains(clusterIndex)
            val fillAlpha = if (isToggled) 0.5f else 0.15f

            drawPath(
                path = path,
                color = color.copy(alpha = fillAlpha),
                style = Stroke(width = 0f),
            )
            drawPath(
                path = path,
                color = color.copy(alpha = fillAlpha),
                style = androidx.compose.ui.graphics.drawscope.Fill,
            )

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 5f),
            )

            if (isToggled && showLabels) {
                val globalIndex = globalClusterOrder.indexOf(pageIndex to clusterIndex)
                if (globalIndex != -1) {
                    val labelNumber = globalIndex + 1

                    var sumX = 0f
                    var sumY = 0f
                    for (p in scaledPoints) {
                        sumX += p.x
                        sumY += p.y
                    }
                    val centerX = sumX / scaledPoints.size
                    val centerY = sumY / scaledPoints.size

                    drawContext.canvas.nativeCanvas.apply {
                        val paint =
                            android.graphics.Paint().apply {
                                this.color = android.graphics.Color.BLACK
                                textSize = 48f
                                isAntiAlias = true
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                setShadowLayer(
                                    6f,
                                    0f,
                                    0f,
                                    android.graphics.Color.WHITE,
                                )
                            }

                        drawText(
                            labelNumber.toString(),
                            centerX,
                            centerY,
                            paint,
                        )
                    }
                }
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
                            val blockPoints = mutableListOf<Point>()
                            for (line in block.lines) {
                                line.cornerPoints?.let { blockPoints.addAll(it) }
                            }
                            if (blockPoints.size >= 3) {
                                val hull = computeConvexHull(blockPoints)
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
