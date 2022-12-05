package com.nagyrobi144.wearable.hrv.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ibi")
data class Ibi(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val value: Int,
    @ColumnInfo val quality: Int,
    @ColumnInfo val timestamp: Long,
)