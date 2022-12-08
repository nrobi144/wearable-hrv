package com.nagyrobi144.wearable.hrv.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "ibi")
data class IbiEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val value: Int,
    @ColumnInfo val timestamp: Long,
)
