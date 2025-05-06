package com.luke.pager.screens.quotescreen.scan.imageprocessing

import android.graphics.Point

fun computeConvexHull(points: List<Point>): List<Point> {
    if (points.size <= 1) return points

    val sorted = points.sortedWith(compareBy({ it.x }, { it.y }))

    fun cross(o: Point, a: Point, b: Point): Int {
        return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x)
    }

    val lower = mutableListOf<Point>()
    for (p in sorted) {
        while (lower.size >= 2 && cross(lower[lower.size - 2], lower[lower.size - 1], p) <= 0) {
            lower.removeAt(lower.size - 1)
        }
        lower.add(p)
    }

    val upper = mutableListOf<Point>()
    for (p in sorted.reversed()) {
        while (upper.size >= 2 && cross(upper[upper.size - 2], upper[upper.size - 1], p) <= 0) {
            upper.removeAt(upper.size - 1)
        }
        upper.add(p)
    }

    lower.removeAt(lower.size - 1)
    upper.removeAt(upper.size - 1)

    return lower + upper
}

