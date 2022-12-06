package com.nagyrobi144.wearable.hrv.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [IbiEntity::class], version = 1)
abstract class IbiDatabase : RoomDatabase() {
    abstract fun ibiDao(): IbiDao
}