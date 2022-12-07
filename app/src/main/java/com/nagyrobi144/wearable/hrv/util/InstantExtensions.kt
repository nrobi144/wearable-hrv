package com.nagyrobi144.wearable.hrv.util

import java.time.Instant
import java.util.*

fun Instant.deviceTimeZone() = atZone(TimeZone.getDefault().toZoneId())

fun Instant.toDayOfYear() = deviceTimeZone().dayOfYear

val currentDay = Instant.now().toDayOfYear()
