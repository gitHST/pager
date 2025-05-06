package com.luke.pager.screens.quotescreen.scan.imageprocessing

import android.graphics.Point

fun angleBetween(p1: Point, p2: Point, p3: Point): Double {
    val v1x = (p1.x - p2.x).toDouble()
    val v1y = (p1.y - p2.y).toDouble()
    val v2x = (p3.x - p2.x).toDouble()
    val v2y = (p3.y - p2.y).toDouble()

    val dot = v1x * v2x + v1y * v2y
    val det = v1x * v2y - v1y * v2x
    val angle = Math.atan2(det, dot) * (180.0 / Math.PI)
    return Math.abs(angle)
}
