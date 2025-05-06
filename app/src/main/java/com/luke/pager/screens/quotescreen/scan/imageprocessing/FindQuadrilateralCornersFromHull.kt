package com.luke.pager.screens.quotescreen.scan.imageprocessing

import android.graphics.Point

fun findQuadrilateralCorners(points: List<Point>): List<Point> {
    if (points.size < 4) return points

    val centerX = points.map { it.x }.average()
    val centerY = points.map { it.y }.average()

    val quadrants = mutableMapOf<String, Point>()

    points.forEach { point ->
        when {
            point.x < centerX && point.y < centerY -> { // top-left
                val current = quadrants["tl"]
                if (current == null || (point.x + point.y) < (current.x + current.y)) {
                    quadrants["tl"] = point
                }
            }
            point.x >= centerX && point.y < centerY -> { // top-right
                val current = quadrants["tr"]
                if (current == null || (point.x - point.y) > (current.x - current.y)) {
                    quadrants["tr"] = point
                }
            }
            point.x >= centerX && point.y >= centerY -> { // bottom-right
                val current = quadrants["br"]
                if (current == null || (point.x + point.y) > (current.x + current.y)) {
                    quadrants["br"] = point
                }
            }
            point.x < centerX && point.y >= centerY -> { // bottom-left
                val current = quadrants["bl"]
                if (current == null || (point.y - point.x) > (current.y - current.x)) {
                    quadrants["bl"] = point
                }
            }
        }
    }

    return listOfNotNull(
        quadrants["tl"],
        quadrants["tr"],
        quadrants["br"],
        quadrants["bl"]
    )
}
