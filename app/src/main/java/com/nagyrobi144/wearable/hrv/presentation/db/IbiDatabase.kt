package com.nagyrobi144.wearable.hrv.presentation.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Ibi::class], version = 1)
abstract class IbiDatabase : RoomDatabase() {
    abstract fun ibiDao(): IbiDao
}