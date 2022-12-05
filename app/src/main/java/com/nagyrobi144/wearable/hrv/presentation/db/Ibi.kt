package com.nagyrobi144.wearable.hrv.presentation.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ibi")
data class Ibi(
    @PrimaryKey val id: Int,
    @ColumnInfo val value: Int,
    @ColumnInfo val quality: Int,
    @ColumnInfo val timestamp: Long,
)