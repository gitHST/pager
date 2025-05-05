package com.luke.pager.screens.quotescreen.imageprocessing

import android.graphics.Rect
import com.google.mlkit.vision.text.Text

data class DBSCANBlockBox(
    val block: Text.TextBlock,
    val rect: Rect,
    val lineRects: List<Rect>
)


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
            minDistanceBetweenLines(box, other) <= eps
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
                minDistanceBetweenLines(box, other) <= eps
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

private fun minDistanceBetweenLines(box1: DBSCANBlockBox, box2: DBSCANBlockBox): Float {
    var minDistance = Float.MAX_VALUE
    for (r1 in box1.lineRects) {
        for (r2 in box2.lineRects) {
            val d = distanceBetweenRectangles(r1, r2)
            if (d < minDistance) {
                minDistance = d
            }
        }
    }
    return minDistance
}

fun minDistanceBetweenClusters(
    cluster1: List<DBSCANBlockBox>,
    cluster2: List<DBSCANBlockBox>
): Float {
    var minDistance = Float.MAX_VALUE
    for (box1 in cluster1) {
        for (box2 in cluster2) {
            val d = minDistanceBetweenLines(box1, box2)
            if (d < minDistance) {
                minDistance = d
            }
        }
    }
    return minDistance
}
