package com.luke.pager.screens.quotescreen.imageprocessing

import android.graphics.Rect
import com.google.mlkit.vision.text.Text

data class DBSCANBlockBox(val block: Text.TextBlock, val rect: Rect)

fun dbscan2D(
    boxes: List<DBSCANBlockBox>,
    eps: Float,
    minPts: Int
): Pair<List<List<DBSCANBlockBox>>, List<DBSCANBlockBox>> {
    val visited = mutableSetOf<DBSCANBlockBox>()
    val noise = mutableListOf<DBSCANBlockBox>()
    val clusters = mutableListOf<MutableList<DBSCANBlockBox>>()

    for (box in boxes) {
        if (visited.contains(box)) continue
        visited.add(box)

        val neighbors = boxes.filter { other ->
            distanceBetweenRectangles(box.rect, other.rect) <= eps
        }

        if (neighbors.size < minPts) {
            noise.add(box)
        } else {
            val cluster = mutableListOf<DBSCANBlockBox>()
            clusters.add(cluster)
            expandCluster2D(box, neighbors, cluster, boxes, visited, eps, minPts)
        }
    }

    return clusters to noise
}

private fun expandCluster2D(
    box: DBSCANBlockBox,
    neighbors: List<DBSCANBlockBox>,
    cluster: MutableList<DBSCANBlockBox>,
    boxes: List<DBSCANBlockBox>,
    visited: MutableSet<DBSCANBlockBox>,
    eps: Float,
    minPts: Int
) {
    cluster.add(box)

    val queue = ArrayDeque(neighbors)
    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        if (!visited.contains(current)) {
            visited.add(current)
            val currentNeighbors = boxes.filter { other ->
                distanceBetweenRectangles(current.rect, other.rect) <= eps
            }
            if (currentNeighbors.size >= minPts) {
                queue.addAll(currentNeighbors)
            }
        }
        if (cluster.none { it == current }) {
            cluster.add(current)
        }
    }
}

private fun distanceBetweenRectangles(r1: Rect, r2: Rect): Float {
    val dx = maxOf(0, maxOf(r1.left, r2.left) - minOf(r1.right, r2.right))
    val dy = maxOf(0, maxOf(r1.top, r2.top) - minOf(r1.bottom, r2.bottom))
    return kotlin.math.sqrt((dx * dx + dy * dy).toFloat())
}
