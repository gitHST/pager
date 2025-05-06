package com.luke.pager.screens.quotescreen.scan.imageprocessing

import android.graphics.Point

fun filterSharpCorners(hull: List<Point>, minAngleDegrees: Double = 30.0): List<Point> {
    if (hull.size < 3) return hull

    val filtered = mutableListOf<Point>()

    for (i in hull.indices) {
        val p1 = hull[(i - 1 + hull.size) % hull.size]
        val p2 = hull[i]
        val p3 = hull[(i + 1) % hull.size]

        val angle = angleBetween(p1, p2, p3)
        if (angle >= minAngleDegrees) {
            filtered.add(p2)
        }
    }

    return filtered
}
