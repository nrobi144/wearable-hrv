package com.nagyrobi144.wearable.hrv.util

import android.util.Log
import com.nagyrobi144.wearable.hrv.feature.TAG
import kotlin.math.abs
import kotlin.math.sqrt

fun List<Int>.rMSSD() = if (size <= 1) null else sqrt((0 until lastIndex).map { index ->
    val diff = this[index + 1] - this[index]
    diff * diff
}.average())


fun List<Int>.normaliseRRIntervals(): List<Int> {
    Log.i(TAG, "before normaliseRRIntervals: $this")

    val normalised = filterIndexed { index, _ ->
        this.isCorrectRRInterval(index)
    }

    return if (normalised.size < size) {
        normalised.normaliseRRIntervals()
    } else {
        Log.i(TAG, "after normaliseRRIntervals: $normalised")
        normalised
    }
}

/**
 * An RR interval differing by more than 150 ms from the 5 adjacent intervals is excluded.
 */
private fun List<Int>.isCorrectRRInterval(index: Int): Boolean {
    val firstIndex = (index - 2).coerceAtLeast(minimumValue = 0)
    val lastIndex = (index + 2).coerceAtMost(maximumValue = lastIndex)

    val number = this[index]
    return subList(firstIndex, lastIndex).all { abs(number - it) <= 150 }
}