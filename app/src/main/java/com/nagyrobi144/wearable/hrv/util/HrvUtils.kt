package com.nagyrobi144.wearable.hrv.util

import kotlin.math.sqrt

fun List<Int>.rMSSD() = if (size <= 1) null else sqrt((0 until lastIndex).map { index ->
    val diff = this[index + 1] - this[index]
    diff * diff
}.average())