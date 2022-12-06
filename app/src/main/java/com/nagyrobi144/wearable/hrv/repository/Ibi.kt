package com.nagyrobi144.wearable.hrv.repository

import com.nagyrobi144.wearable.hrv.db.IbiEntity
import java.time.Instant


data class Ibi(
    val value: Int,
    val quality: Int,
    val instant: Instant,
) {
    val timestamp = instant.toEpochMilli()
}

fun Ibi.toIbiEntity() = IbiEntity(
    value = value,
    quality = quality,
    timestamp = instant.toEpochMilli(),
)

fun IbiEntity.toIbi() = Ibi(
    value = value,
    quality = quality,
    instant = Instant.ofEpochMilli(timestamp),
)
