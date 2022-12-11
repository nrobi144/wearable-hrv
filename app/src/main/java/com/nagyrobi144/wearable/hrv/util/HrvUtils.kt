package com.nagyrobi144.wearable.hrv.util

import kotlin.math.abs
import kotlin.math.sqrt

fun List<Int>.rMSSD() = if (size <= 1) null else sqrt((0 until lastIndex).map { index ->
    val diff = this[index + 1] - this[index]
    diff * diff
}.average())


fun List<Int>.normaliseRRIntervals(): List<Int> {
    val normalised = filterIndexed { index, _ ->
        this.isCorrectRRInterval(index)
    }

    return normalised
}

/**
 * An RR interval differing by more than 150 ms from the 5 adjacent intervals is excluded.
 */
private fun List<Int>.isCorrectRRInterval(index: Int): Boolean {
    val firstIndex = (index - 5).coerceAtLeast(minimumValue = 0)
    val lastIndex = (index + 5).coerceAtMost(maximumValue = lastIndex)

    val number = this[index]
    return subList(firstIndex, lastIndex).all { abs(number - it) <= 150 }
}