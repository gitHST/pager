package com.luke.pager.screens.quotescreen.imageprocessing

fun kMeans1D(
    points: List<Float>,
    k: Int,
    maxIterations: Int = 100
): List<List<Float>> {
    if (points.isEmpty()) return emptyList()

    // start with random initial centers
    var centers = points.shuffled().take(k).toMutableList()

    repeat(maxIterations) {
        // assign each point to the closest center
        val clusters = List(k) { mutableListOf<Float>() }
        for (point in points) {
            val closestIndex = centers.indices.minByOrNull { i -> kotlin.math.abs(point - centers[i]) } ?: 0
            clusters[closestIndex].add(point)
        }

        // recalculate centers
        val newCenters = clusters.map { cluster ->
            if (cluster.isNotEmpty()) cluster.average().toFloat() else 0f
        }

        // check for convergence
        if (centers.zip(newCenters).all { (old, new) -> old == new }) {
            return clusters
        }

        centers = newCenters.toMutableList()
    }

    // final clusters
    val clusters = List(k) { mutableListOf<Float>() }
    for (point in points) {
        val closestIndex = centers.indices.minByOrNull { i -> kotlin.math.abs(point - centers[i]) } ?: 0
        clusters[closestIndex].add(point)
    }
    return clusters
}
