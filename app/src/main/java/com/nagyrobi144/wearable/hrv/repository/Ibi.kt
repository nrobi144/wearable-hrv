package com.nagyrobi144.wearable.hrv.repository

import com.nagyrobi144.wearable.hrv.db.IbiEntity
import java.time.Instant


data class Ibi(
    val value: Int,
    val instant: Instant,
) {
    val timestamp = instant.toEpochMilli()
}

fun Ibi.toIbiEntity() = IbiEntity(
    value = value,
    timestamp = instant.toEpochMilli(),
)

fun IbiEntity.toIbi() = Ibi(
    value = value,
    instant = Instant.ofEpochMilli(timestamp),
)
