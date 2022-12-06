package com.nagyrobi144.wearable.hrv.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "ibi")
data class Ibi(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val value: Int,
    @ColumnInfo val quality: Int,
    @ColumnInfo val timestamp: Long,
) {
    override fun toString(): String {
        return "Ibi(value=$value, quality=$quality, time=${Instant.ofEpochMilli(timestamp)})"
    }
}