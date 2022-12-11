package com.nagyrobi144.wearable.hrv.util

import com.nagyrobi144.wearable.hrv.repository.Ibi
import java.time.Instant
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

fun Instant.deviceTimeZone() = atZone(TimeZone.getDefault().toZoneId())

fun Instant.toDayOfYear() = deviceTimeZone().dayOfYear

val currentDay = GregorianCalendar(TimeZone.getDefault()).toInstant().toDayOfYear()

fun List<Ibi>.filterTodaysData() = filter { it.instant.toDayOfYear() == currentDay }

fun createEpochsFrom(
    earliestTimestamp: Long,
    latestTimestamp: Long,
    epochDurationInMillis: Duration = 5.minutes
) =
    (earliestTimestamp..latestTimestamp step epochDurationInMillis.inWholeMilliseconds)
        .map { Instant.ofEpochMilli(it) }
